group = "org.jboss.gm.common"

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compile("org.apache.commons:commons-lang3:3.8.1")
    compile("commons-io:commons-io:2.6")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    compile("org.commonjava.maven.ext:pom-manipulation-io:3.5.1")
    implementation(gradleApi())
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}