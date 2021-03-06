/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.psi.impl.search;

import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.DelegatingGlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author max
 */
public class JavaSourceFilterScope extends DelegatingGlobalSearchScope {
  private final @Nullable ProjectFileIndex myIndex;
  private final boolean myIncludeVersions;

  public JavaSourceFilterScope(@NotNull GlobalSearchScope delegate) {
    this(delegate, false);
  }

  public JavaSourceFilterScope(@NotNull GlobalSearchScope delegate, boolean includeVersions) {
    super(delegate);
    Project project = getProject();
    myIndex = project == null ? null : ProjectRootManager.getInstance(project).getFileIndex();
    myIncludeVersions = includeVersions;
  }

  @Override
  public boolean contains(@NotNull final VirtualFile file) {
    if (!super.contains(file)) {
      return false;
    }

    if (myIndex == null) {
      return false;
    }

    if (JavaClassFileType.INSTANCE == file.getFileType()) {
      return myIndex.isInLibraryClasses(file) && (myIncludeVersions || !isVersioned(file));
    }

    return myIndex.isInSourceContent(file) ||
           myBaseScope.isForceSearchingInLibrarySources() && myIndex.isInLibrarySource(file);
  }

  private static boolean isVersioned(VirtualFile file) {
    return file.getPath().contains("/META-INF/versions/");
  }
}