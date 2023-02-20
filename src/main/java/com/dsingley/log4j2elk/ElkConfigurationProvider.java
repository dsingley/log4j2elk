package com.dsingley.log4j2elk;

@FunctionalInterface
public interface ElkConfigurationProvider {

    ElkConfiguration getElkConfiguration();
}
