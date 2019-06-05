/*
 * Copyright 2019 by Daan van den Heuvel.
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
package dataflow.soot;

import soot.Main;
import soot.PackManager;
import soot.Transform;

/**
 * Class for verifying if we can use the Soot library to construct dataflow graphs.
 *
 * @author Daan
 */
public class TestSoot {

  public static void main(String[] args) {
    // Inject the analysis tagger into Soot
    PackManager.v().getPack("jtp").add(new Transform("jpt.myanalysistagger", Foo.instance()));
    // Invoke soot.Main with arguments given
    Main.main(args);
  }

}
