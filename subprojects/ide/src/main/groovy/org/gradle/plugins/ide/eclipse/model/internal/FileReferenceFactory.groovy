/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.plugins.ide.eclipse.model.internal

import org.gradle.plugins.ide.eclipse.model.FileReference

class FileReferenceFactory {
    private final Map<String, File> variables = [:]

    /**
     * Adds a path variable
     */
    void addPathVariable(String name, File dir) {
        variables[name] = dir
    }

    /**
     * Creates a reference to the given file.
     */
    FileReference file(File file) {
        def path
        def usedVar = false
        for (entry in variables.entrySet()) {
            def rootDirPath = entry.value.absolutePath
            def filePath = file.absolutePath
            if (filePath == rootDirPath) {
                path = entry.key
                usedVar = true
                break
            }
            if (filePath.startsWith(rootDirPath + File.separator)) {
                int len = rootDirPath.length()
                path = entry.key + filePath.substring(len)
                usedVar = true
                break
            }
        }
        path = path ?: PathUtil.normalizePath(file.absolutePath)
        return new FileReferenceImpl(file, path, usedVar)
    }

    /**
     * Creates a reference to the given path.
     */
    FileReference file(String path) {
        new FileReferenceImpl(new File(path), path, false)
    }

    /**
     * Creates a reference to the given path containing a variable reference.
     */
    FileReference var(String path) {
        for (entry in variables.entrySet()) {
            def prefix = "$entry.key/"
            if (path.startsWith(prefix)) {
                def file = new File(entry.value, path.substring(prefix.length()))
                return new FileReferenceImpl(file, path, true)
            }
        }
        return file(path)
    }

    private static class FileReferenceImpl implements FileReference {
        final File file
        final String path
        final boolean relativeToPathVariable

        FileReferenceImpl(File file, String path, boolean relativeToPathVariable) {
            this.file = file
            this.path = path
            this.relativeToPathVariable = relativeToPathVariable
        }
    }
}