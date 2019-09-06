/*
 * Copyright 2018 by Daan van den Heuvel.
 *
 * This file is part of JavaForger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package configuration;

import java.util.function.Consumer;

import templateInput.ClassContainer;

/**
 * Functional interface to implement a consumer of {@link ClassContainer} so that the input parameters can be changed after parsing.
 *
 * @author Daan
 */
public interface ClassContainerAdjuster extends Consumer<ClassContainer> {
  // Class is empty because Consumer already defines required methods. This class is mainly needed to ensure type safety.
}
