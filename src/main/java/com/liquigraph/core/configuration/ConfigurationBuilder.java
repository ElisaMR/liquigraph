package com.liquigraph.core.configuration;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.liquigraph.core.configuration.validators.ExecutionModeValidator;
import com.liquigraph.core.configuration.validators.MandatoryOptionValidator;

import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.liquigraph.core.configuration.RunMode.RUN_MODE;
import static java.lang.String.format;

/**
 * Fluent {@link com.liquigraph.core.configuration.Configuration} builder.
 * It also validates configuration parameters.
 */
public final class ConfigurationBuilder {

    private String masterChangelog;
    private String uri;
    private Optional<String> username = absent();
    private Optional<String> password = absent();
    private ExecutionContexts executionContexts = ExecutionContexts.DEFAULT_CONTEXT;
    private ExecutionMode executionMode;

    private MandatoryOptionValidator mandatoryOptionValidator = new MandatoryOptionValidator();
    private ExecutionModeValidator executionModeValidator = new ExecutionModeValidator();

    /**
     * Specifies the location of the master changelog file.
     * Please note that this location should point to a readable Liquigraph changelog file.
     * @param masterChangelog Liquigraph changelog
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withMasterChangelogLocation(String masterChangelog) {
        this.masterChangelog = masterChangelog;
        return this;
    }

    /**
     * Specifies the connection URI of the graph database instance.
     * If the graph database is embedded, please use 'file://' as a prefix.
     * Otherwise, only 'http://' and 'https://' are supported at the moment.
     *
     * @param uri connection URI
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Specifies the username allowed to connect to the remote graph database instance.
     * @param username username
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withUsername(String username) {
        this.username = fromNullable(username);
        return this;
    }

    /**
     * Specifies the password allowed to connect to the remote graph database instance.
     * @param password password
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withPassword(String password) {
        this.password = fromNullable(password);
        return this;
    }

    /**
     * @see com.liquigraph.core.configuration.ConfigurationBuilder#withExecutionContexts(java.util.Collection)
     */
    public ConfigurationBuilder withExecutionContexts(String... executionContexts) {
        return withExecutionContexts(newArrayList(executionContexts));
    }

    /**
     * Specifies one or more execution contexts.
     * 
     * @param executionContexts non-nullable execution contexts
     * @return itself for chaining purposes
     */
    public ConfigurationBuilder withExecutionContexts(Collection<String> executionContexts) {
        if (!executionContexts.isEmpty()) {
            this.executionContexts = new ExecutionContexts(executionContexts);
        }
        return this;
    }

    public ConfigurationBuilder withRunMode() {
        this.executionMode = RUN_MODE;
        return this;
    }

    public ConfigurationBuilder withDryRunMode(Path outputDirectory) {
        this.executionMode = new DryRunMode(outputDirectory);
        return this;
    }

    /**
     * Builds a {@link com.liquigraph.core.configuration.Configuration} instance after validating the specified
     * parameters.
     *
     * @return Liquigraph configuration
     */
    public Configuration build() {
        Collection<String> errors = newLinkedList();
        errors.addAll(mandatoryOptionValidator.validate(masterChangelog, uri));
        errors.addAll(executionModeValidator.validate(executionMode));

        if (!errors.isEmpty()) {
            throw new RuntimeException(formatErrors(errors));
        }
        return new Configuration(
            masterChangelog,
            uri,
            username,
            password,
            executionContexts,
            executionMode
        );
    }

    private String formatErrors(Collection<String> errors) {
        String separator = "\n\t - ";
        return format("%s%s", separator, Joiner.on(separator).join(errors));
    }
}
