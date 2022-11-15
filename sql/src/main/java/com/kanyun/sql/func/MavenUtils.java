package com.kanyun.sql.func;

import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.*;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Pattern;

public class MavenUtils {

    private static final Logger log = LoggerFactory.getLogger(MavenUtils.class);

    public static final String SESSION_CHECKS = "updateCheckManager.checks";

    private static Settings settings;

    private RemoteRepository localRepository;

    public Settings initSetting() {
        SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
        DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();

        String mavenHome = System.getenv("M2_HOME");
        if (mavenHome != null) {
            File globalSettingsFile = new File(mavenHome + "/conf/settings.xml");
            if (globalSettingsFile.exists()) {
                request.setGlobalSettingsFile(globalSettingsFile);
            }
        } else {
            log.warn("Environment variable M2_HOME is not set");
        }

        request.setSystemProperties(System.getProperties());

        try {
            settings = settingsBuilder.build(request).getEffectiveSettings();
        } catch (SettingsBuildingException e) {
            throw new RuntimeException(e);
        }
        if (settings.getLocalRepository() == null) {
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                settings.setLocalRepository(userHome + "/.m2/repository");
            } else {
                log.error("Cannot find maven local repository");
            }
        }

        return settings;
    }

    /**
     * 获取远程仓库地址
     *
     * @return
     */
    public Collection<RemoteRepository> initRemoteRepositoriesForRequest() {
        Collection<RemoteRepository> remoteRepos = new HashSet();

//        获取Maven远程仓库地址渠道1
        Collection<RemoteRepository> extraRepositories = initGlobalRemoteRepositories();
        for (RemoteRepository repo : extraRepositories) {
            remoteRepos.add(resolveMirroredRepo(repo));
        }


//        获取Maven远程仓库地址渠道2
        for (RemoteRepository repo : initProjectRemoteRepositories(null)) {
            remoteRepos.add(resolveMirroredRepo(repo));
        }
        return remoteRepos;
    }

    /**
     * 初始化项目中配置的远程仓库,读取的是项目中的pom.xml中配置的仓库信息
     *
     * @param mavenProject
     * @return
     */
    private Collection<RemoteRepository> initProjectRemoteRepositories(MavenProject mavenProject) {
        Collection<RemoteRepository> reps = new HashSet();
        reps.add(newCentralRepository());
        if (mavenProject != null) {
            reps.addAll(mavenProject.getRemoteProjectRepositories());
        }

        RemoteRepository localRepo = newLocalRepository();
        if (localRepo != null) {
            localRepository = localRepo;
        }
        return reps;
    }

    private RemoteRepository newLocalRepository() {
        String localRepoDir = settings.getLocalRepository();
        File m2RepoDir = new File(localRepoDir);
        try {
            if (!m2RepoDir.exists()) {
                log.warn("The local repository directory " + localRepoDir + " doesn't exist. Creating it.");
                m2RepoDir.mkdirs();
            }
            String localRepositoryUrl = m2RepoDir.toURI().toURL().toExternalForm();
            return new RemoteRepository.Builder("local", "default", localRepositoryUrl).build();
        } catch (Exception e) {
            try {
                log.warn("Cannot use directory " + localRepoDir + " as local repository.", e);
                localRepoDir = getTmpDirectory().getAbsolutePath();
                log.warn("Using the temporary directory " + localRepoDir + " as local repository");
                m2RepoDir = new File(localRepoDir);
                String localRepositoryUrl = m2RepoDir.toURI().toURL().toExternalForm();
                return new RemoteRepository.Builder("local", "default", localRepositoryUrl).build();
            } catch (Exception e1) {
                log.warn("Cannot create a local repository in " + localRepoDir, e1);
            }
        }
        return null;
    }

    public static File getTmpDirectory() {
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        File f = new File(tmp, "_kie_repo_" + UUID.randomUUID().toString());
        //files.add( f );
        if (f.exists()) {
            if (f.isFile()) {
                throw new IllegalStateException("The temp directory exists as a file. Nuke it now !");
            }
            deleteDir(f);
            f.mkdir();
        } else {
            f.mkdir();
        }
        return f;
    }

    private static void deleteDir(File dir) {
        // Will throw RuntimeException is anything fails to delete
        String[] children = dir.list();
        for (String child : children) {
            File file = new File(dir,
                    child);
            if (file.isFile()) {
                deleteFile(file);
            } else {
                deleteDir(file);
            }
        }

        deleteFile(dir);
    }

    private static void deleteFile(File file) {
        // This will attempt to delete a file 5 times, calling GC and Sleep between each iteration
        // Sometimes windows takes a while to release a lock on a file.
        // Throws an exception if it fails to delete
        if (!file.delete()) {
            int count = 0;
            while (!file.delete() && count++ < 5) {
                System.gc();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    throw new RuntimeException("This should never happen");
                }
            }
        }

        if (file.exists()) {
            try {
                throw new RuntimeException("Unable to delete file:" + file.getCanonicalPath());
            } catch (IOException e) {
                throw new RuntimeException("Unable to delete file", e);
            }
        }
    }

    RemoteRepository newCentralRepository() {
        return new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
    }

    /**
     * 初始化全局远程maven仓库,读取的是运行环境中settings.xml中配置的仓库信息
     *
     * @return
     */
    private Collection<RemoteRepository> initGlobalRemoteRepositories() {
        Collection<RemoteRepository> extraRepositories = new HashSet<RemoteRepository>();
        for (Profile profile : settings.getProfiles()) {
            if (isProfileActive(profile)) {
                for (Repository repository : profile.getRepositories()) {
                    extraRepositories.add(toRemoteRepositoryBuilder(settings,
                            repository).build());
                }
                for (Repository repository : profile.getPluginRepositories()) {
                    extraRepositories.add(toRemoteRepositoryBuilder(settings,
                            repository).build());
                }
            }
        }
        return extraRepositories;
    }

    /**
     * maven指定环境是否被激活
     *
     * @param profile
     * @return
     */
    private boolean isProfileActive(Profile profile) {
        return settings.getActiveProfiles().contains(profile.getId()) ||
                (profile.getActivation() != null && profile.getActivation().isActiveByDefault());
    }


    /**
     * 远程仓库镜像
     *
     * @param settings
     * @param repo
     * @return
     */
    public RemoteRepository resolveMirroredRepo(Settings settings, RemoteRepository repo) {
        for (Mirror mirror : settings.getMirrors()) {
            if (isMirror(repo, mirror.getMirrorOf())) {
                return toRemoteRepositoryBuilder(settings,
                        mirror.getId(),
                        mirror.getLayout(),
                        mirror.getUrl()).build();
            }
        }
        return repo;
    }

    public RemoteRepository resolveMirroredRepo(RemoteRepository repo) {
        for (Mirror mirror : settings.getMirrors()) {
            if (isMirror(repo, mirror.getMirrorOf())) {
                return toRemoteRepositoryBuilder(settings,
                        mirror.getId(),
                        mirror.getLayout(),
                        mirror.getUrl()).build();
            }
        }
        return repo;
    }

    /**
     * 判断是否是镜像仓库
     *
     * @param repo
     * @param mirrorOf
     * @return
     */
    private boolean isMirror(RemoteRepository repo,
                             String mirrorOf) {
        return mirrorOf.equals("*") ||
                (mirrorOf.equals("external:*") && !repo.getUrl().startsWith("file:")) ||
                (mirrorOf.contains("external:*") && !repo.getUrl().startsWith("file:") && !mirrorOf.contains("!" + repo.getId())) ||
                (mirrorOf.startsWith("*") && !mirrorOf.contains("!" + repo.getId())) ||
                (!mirrorOf.startsWith("*") && !mirrorOf.contains("external:*") && mirrorOf.contains(repo.getId()));
    }

    private static RemoteRepository.Builder toRemoteRepositoryBuilder(Settings settings,
                                                                      Repository repository) {
        RemoteRepository.Builder remoteBuilder = toRemoteRepositoryBuilder(settings,
                repository.getId(),
                repository.getLayout(),
                repository.getUrl());
        setPolicy(remoteBuilder, repository.getSnapshots(),
                true);
        setPolicy(remoteBuilder, repository.getReleases(),
                false);
        return remoteBuilder;
    }

    private static void setPolicy(RemoteRepository.Builder builder,
                                  RepositoryPolicy policy,
                                  boolean snapshot) {
        if (policy != null) {
            org.eclipse.aether.repository.RepositoryPolicy repoPolicy =
                    new org.eclipse.aether.repository.RepositoryPolicy(policy.isEnabled(),
                            policy.getUpdatePolicy(),
                            policy.getChecksumPolicy());
            if (snapshot) {
                builder.setSnapshotPolicy(repoPolicy);
            } else {
                builder.setReleasePolicy(repoPolicy);
            }
        }
    }

    private static RemoteRepository.Builder toRemoteRepositoryBuilder(Settings settings,
                                                                      String id,
                                                                      String layout,
                                                                      String url) {
        final Proxy activeProxy = settings.getActiveProxy();
        RemoteRepository.Builder remoteBuilder = new RemoteRepository.Builder(id,
                layout,
                url);
        Server server = settings.getServer(id);
        if (server != null) {
            remoteBuilder.setAuthentication(new AuthenticationBuilder().addUsername(server.getUsername())
                    .addPassword(server.getPassword())
                    .build());
        }

        if (activeProxy != null) {
            if (null == activeProxy.getNonProxyHosts()) {
                remoteBuilder.setProxy(getActiveAetherProxyFromSettings(settings));
            } else if (!repositoryUrlMatchNonProxyHosts(settings.getActiveProxy().getNonProxyHosts(), remoteBuilder.build().getUrl())) {
                remoteBuilder.setProxy(getActiveAetherProxyFromSettings(settings));
            }
        }
        return remoteBuilder;
    }

    private static org.eclipse.aether.repository.Proxy getActiveAetherProxyFromSettings(final Settings settings) {
        return new org.eclipse.aether.repository.Proxy(settings.getActiveProxy().getProtocol(),
                settings.getActiveProxy().getHost(),
                settings.getActiveProxy().getPort(),
                new AuthenticationBuilder()
                        .addUsername(settings.getActiveProxy().getUsername())
                        .addPassword(settings.getActiveProxy().getPassword())
                        .build());
    }

    private static boolean repositoryUrlMatchNonProxyHosts(String nonProxyHosts, String artifactURL) {
        // Replace * with .* so nonProxyHosts comply with pattern matching syntax
        String nonProxyHostsRegexp = nonProxyHosts.replace("*", ".*");
        try {
            Pattern p = Pattern.compile(nonProxyHostsRegexp);
            URL url = new URL(artifactURL);
            return p.matcher(url.getHost()).find();
        } catch (MalformedURLException e) {
            log.warn("Failed to parse URL proxy {}, cause {}", artifactURL, e);
            return false;
        }
    }
}
