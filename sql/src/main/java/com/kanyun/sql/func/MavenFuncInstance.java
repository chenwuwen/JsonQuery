package com.kanyun.sql.func;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.jar.JarFile;

/**
 * Maven形式加载依赖
 * 分两步判断:
 * 1.先在当前包中查询依赖,如果存在则不继续找
 * 2.如果前面未找到,则在当前运行操作系统上查询maven相关设置,查找依赖
 */
public class MavenFuncInstance extends AbstractFuncSource {

    private static final Logger log = LoggerFactory.getLogger(MavenFuncInstance.class);

    @Override
    public void loadJar(String... args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Maven方法依赖需提供3个参数：①.groupId ②.artifactId ③.version");
        String groupId = args[0];
        String artifactId = args[1];
        String version = args[2];
//        定义jar文件路径
        String jarFilePath = "";
//        此处是当maven依赖在当前的包中的情况
        jarFilePath = loadJarFormSelf(groupId, artifactId, version);
        if (jarFilePath.trim().length() == 0) {
//            说明指定的maven依赖不在当前的包中,接下来需要我们访问当前系统环境的maven信息
            jarFilePath = loadJarFormMaven(groupId, artifactId, version);
        }
        if (jarFilePath.trim().length() == 0) {
            String msg = String.format("未找到指定的maven依赖:group: '%s', name: '%s', version: '%s'", groupId, artifactId, version);
            throw new IllegalArgumentException(msg);
        }
        File file = new File(jarFilePath);
        JarFile jarFile = new JarFile(file);
        parseJar(jarFile, null);

    }

    /**
     * 从包本身获取依赖的路径
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     * @throws Exception
     */
    public String loadJarFormSelf(String groupId, String artifactId, String version) throws Exception {
        String jarFilePath = "";
        String configFileName = String.format("META-INF/maven/%s/%s/pom.properties", groupId, artifactId);
        for (ClassLoader currentClassLoader = this.getClass().getClassLoader(); currentClassLoader != null; currentClassLoader = currentClassLoader.getParent()) {
//            指定的配置文件在打好的包中只存在一个(groupId-artifactId相同的多个版本,只打进一个)
//            Enumeration<URL> resources = currentClassLoader.getResources(configFileName);
            URL resource = currentClassLoader.getResource(configFileName);
            if (resource != null && resource.getProtocol().equals("jar")) {
                Properties properties = new Properties();
                properties.load(resource.openStream());
//            注意此处为判断依赖版本,因为当前maven依赖已在包中(但是包中的版本可能是旧版本,因此这里再进行版本判断)
                if (properties.getProperty("version").equals(version)) {
//                    依赖版本不正确进入下一次循环
                    continue;
                }
//                得到properties配置文件,此配置文件在jar包中,因此他的文件路径中存在叹号
                File configFile = new File(resource.getFile());
//                根据配置文件的路径获取父级路径(得到父级.jar路径,注意此时路径结尾包含叹号!,路径头部包含file:/)
                String filePath = configFile.getParentFile().getParentFile().getParentFile().getParentFile().getParent();
//                去除filePath前缀(file:/)
                URL url = new URL(filePath);
//                去除路径结尾的叹号.得到jar路径
                jarFilePath = url.getPath().substring(0, url.getPath().length() - 1);

            }
        }
        return jarFilePath;
    }

    /**
     * 从当前运行环境的maven获取依赖
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     * @throws Exception
     */
    public String loadJarFormMaven(String groupId, String artifactId, String version) throws Exception {
        MavenUtils mavenUtils = new MavenUtils();
        Settings settings = mavenUtils.initSetting();
        String localRepositoryPath = settings.getLocalRepository();
        String artifactName = groupId + ":" + artifactId + ":" + version;
        Artifact artifact = new DefaultArtifact(artifactName);
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        for (RemoteRepository repo : mavenUtils.initRemoteRepositoriesForRequest()) {
            artifactRequest.addRepository(repo);
        }
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.addService(TransporterFactory.class, WagonTransporterFactory.class);
//        locator.setServices( WagonProvider.class, new ManualWagonProvider() );
        RepositorySystem system = locator.getService(RepositorySystem.class);
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, new LocalRepository(localRepositoryPath)));
//        session.setOffline(offline);
        session.setSystemProperties(System.getProperties());
        Object sessionChecks = null;
        boolean isSnapshot = artifactName.endsWith("-SNAPSHOT");
        if (artifactName.endsWith("-SNAPSHOT")) {
            // ensure to always update snapshots
            sessionChecks = session.getData().get(MavenUtils.SESSION_CHECKS);
            session.getData().set(MavenUtils.SESSION_CHECKS, null);
        }
        try {
            log.info("准备获取构件[{}]", artifactName);
            ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
            if (artifactResult.isResolved()) {
                return artifactResult.getArtifact().getFile().getAbsolutePath();
            }
        } catch (ArtifactResolutionException e) {
            log.error("Unable to resolve artifact: " + artifactName);
        } finally {
            if (sessionChecks != null) {
                session.getData().set(MavenUtils.SESSION_CHECKS, sessionChecks);
            }
            session.setReadOnly();
        }
        return "";
    }

}
