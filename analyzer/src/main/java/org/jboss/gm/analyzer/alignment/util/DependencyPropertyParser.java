package org.jboss.gm.analyzer.alignment.util;

import java.util.function.Predicate;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectRef;
import org.gradle.api.InvalidUserDataException;

/**
 * Utility class that is meant to parse properties like dependencyOverride and dependencyExclusion
 *
 * See section "Exclusions and Overrides" of
 * https://release-engineering.github.io/pom-manipulation-ext/guide/dep-manip.html
 */
public final class DependencyPropertyParser {

    private DependencyPropertyParser() {
    }

    public static Result parse(String key) {
        if (!key.contains("@")) {
            throw new InvalidUserDataException(
                    "Property '" + key + "' is not a properly formatted key since it does not contain '@'");
        }

        final String[] artifactAndModule = key.split("@");
        if (artifactAndModule.length != 2) {
            throw new InvalidUserDataException(
                    "Property '" + key + "' is not a properly formatted key since it is not properly split by '@'");
        }

        return new ResultImpl(SimpleProjectRef.parse(artifactAndModule[0]), createMatchesModulePredicate(artifactAndModule[1]));
    }

    private static Predicate<ProjectRef> createMatchesModulePredicate(String moduleValue) {
        if ("*".equals(moduleValue)) {
            return (p) -> true;
        }

        final ProjectRef moduleGA = SimpleProjectRef.parse(moduleValue);
        return (p) -> p.matches(moduleGA);
    }

    public interface Result {

        ProjectRef getDependency();

        boolean matchesModule(ProjectRef projectRef);
    }

    private static class ResultImpl implements Result {
        private final ProjectRef dependency;
        private final Predicate<ProjectRef> matchesModulePredicate;

        ResultImpl(ProjectRef dependency, Predicate<ProjectRef> matchesModulePredicate) {
            this.dependency = dependency;
            this.matchesModulePredicate = matchesModulePredicate;
        }

        @Override
        public ProjectRef getDependency() {
            return dependency;
        }

        @Override
        public boolean matchesModule(ProjectRef projectRef) {
            return matchesModulePredicate.test(projectRef);
        }
    }
}
