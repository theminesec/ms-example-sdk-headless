@file:Suppress("LocalVariableName", "UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //mavenLocal()

        // MineSec's maven registry (client-facing: holds the formal/released SDK,
        // i.e. what customers consume, artifactId `headless-stage` / `headless`)
        maven {
            val MINESEC_REGISTRY_LOGIN: String? by settings
            val MINESEC_REGISTRY_TOKEN: String? by settings

            requireNotNull(MINESEC_REGISTRY_LOGIN) {
                """
                    Please set your MineSec Github credential in `gradle.properties`.
                    On local machine,
                    ** DO NOT **
                    ** DO NOT **
                    ** DO NOT **
                    Do not put it in the project's file. (and accidentally commit and push)
                    ** DO **
                    Do set it in your machine's global (~/.gradle/gradle.properties)
                """.trimIndent()
            }
            requireNotNull(MINESEC_REGISTRY_TOKEN)
            println("MS GPR: $MINESEC_REGISTRY_LOGIN")

            name = "MineSecMavenClientRegistry"
            url = uri("https://maven.pkg.github.com/theminesec/ms-registry-client")
            credentials {
                username = MINESEC_REGISTRY_LOGIN
                password = MINESEC_REGISTRY_TOKEN
            }
        }

        // MineSec's internal QA registry, only needed when building against an
        // unreleased RC (artifactId `headless-stage-rc` / `headless-rc`).
        // Only wired up when explicitly requested (-PsdkChannel=rc), so the
        // default build (formal SDK from the client registry above) never needs
        // these credentials.
        if (providers.gradleProperty("sdkChannel").orNull == "rc") {
            maven {
                val MS_INTERNAL_REGISTRY_USER: String? by settings
                val MS_INTERNAL_REGISTRY_TOKEN: String? by settings

                requireNotNull(MS_INTERNAL_REGISTRY_USER) {
                    "Building against an RC SDK (-PsdkChannel=rc) requires " +
                        "MS_INTERNAL_REGISTRY_USER / MS_INTERNAL_REGISTRY_TOKEN " +
                        "(set in ~/.gradle/gradle.properties locally, or as CI secrets)."
                }
                requireNotNull(MS_INTERNAL_REGISTRY_TOKEN)

                name = "MineSecMavenInternalRegistry"
                url = uri("https://maven.pkg.github.com/theminesec/ms-registry-internal")
                credentials {
                    username = MS_INTERNAL_REGISTRY_USER
                    password = MS_INTERNAL_REGISTRY_TOKEN
                }
            }
        }
    }
}

rootProject.name = "MS Example - Headless SDK"
include(
    ":landing",
    ":compose",
    ":compose-screen",
    ":xml",
    ":xml-java",
    ":msa-ui"
)
