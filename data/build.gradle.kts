plugins {
    bourbon.`android-library-conventions`
}

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments.putAll(
                    setOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true"
                    )
                )
            }
        }
    }
}

dependencies {

}