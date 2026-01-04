module sql {
    requires calcite.core;
    requires org.slf4j;
    requires avatica.core;
    requires com.google.common;
    requires org.apache.commons.lang3;
    requires java.sql;
    requires org.reflections;
    requires com.google.gson;
    requires org.apache.maven.resolver.spi;
    requires aether.transport.file;
    requires aether.transport.http;
    requires aether.transport.wagon;
    requires org.apache.maven.resolver;
    requires aether.connector.basic;
    requires org.apache.maven.resolver.impl;
    requires maven.settings;
    requires maven.resolver.provider;
    requires calcite.linq4j;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.pool2;
    requires maven.settings.builder;
    requires org.apache.maven.resolver.util;
    requires maven.core;
    requires calcite.server;
}