package org.jboss.gm.manipulation.actions;

import static org.apache.commons.lang.StringUtils.isEmpty;

import org.aeonbits.owner.ConfigCache;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.credentials.HttpHeaderCredentials;
import org.gradle.api.logging.Logger;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.authentication.http.HttpHeaderAuthentication;
import org.jboss.gm.common.Configuration;
import org.jboss.gm.common.logging.GMLogger;

/**
 * Adds a publishing repository specific to PNC environment.
 * <p>
 * System properties "AProxDeployUrl" and "accessToken" has to be defined during build.
 * <p>
 * Is equivalent to following gradle snippet:
 *
 * <pre>
 * publishing {
 *         repositories {
 *             maven {
 *                 name = "PNC"
 *                 url = System.getProperty('AProxDeployUrl')
 *                 credentials(HttpHeaderCredentials) {
 *                     name = "Authorization"
 *                     value = "Bearer " + System.getProperty('accessToken')
 *                 }
 *                 authentication {
 *                     header(HttpHeaderAuthentication)
 *                 }
 *             }
 *         }
 *     }
 * </pre>
 */
public class PublishingRepositoryAction implements Action<Project> {

    private final Logger logger = GMLogger.getLogger(getClass());

    @Override
    public void execute(Project project) {
        // disable existing publishing tasks but make sure we keep ours
        project.afterEvaluate(p -> {
            p.getTasks().stream()
                    .filter(t -> t.getName().startsWith("publish") && t.getName().endsWith("Repository")
                            && !t.getName().contains("Manipulator"))
                    .forEach(t -> {
                        logger.info("Disabling publishing task " + t.getName());
                        t.setEnabled(false);
                    });
        });

        if (!project.getPluginManager().hasPlugin("maven-publish")) {
            logger.warn("Cannot configure publishing repository, maven-publish plugin was not detected.");
            return;
        }

        Configuration config = ConfigCache.getOrCreate(Configuration.class);

        if (isEmpty(config.deployUrl())) {
            logger.warn("Publishing URL was not configured.");
            return;
        }

        if (isEmpty(config.accessToken())) {
            logger.warn("No authentication token was configured.");
        }

        project.getExtensions().getByType(PublishingExtension.class).getRepositories().maven(repository -> {
            repository.setName("Manipulator Publishing Repository");
            repository.setUrl(config.deployUrl());
            if (!isEmpty(config.accessToken())) {
                repository.credentials(HttpHeaderCredentials.class, cred -> {
                    cred.setName("Authorization");
                    cred.setValue("Bearer " + config.accessToken());
                });
                repository.getAuthentication().create("header", HttpHeaderAuthentication.class);
            }
        });
    }
}
