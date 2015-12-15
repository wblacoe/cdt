/* Copyright 2010 Speech and Language Technologies Lab, The Ohio State University
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

package sprwikiwordrelatedness;

/**
 * Class for linking relative rankings.
 * <p>
 * Used with the {@link Spearman} class for correlation calculation.
 * 
 * @author weale
 * @version 1.0
 * 
 */
public class Ranking {
  private int originalID;
  private double value;
  private double rank;

 /**
  * Constructor.
  * <p>
  * Sets the id value and the raw value. Rank value is set using the appropriate modifier method.
  */
  public Ranking(int id, double d) {
    originalID = id;
    value = d;
  }

 /**
  * Accessor for the id value
  * 
  * @return data point id
  */
  public int getID() {
    return originalID;
  }

 /**
  * Accessor for the raw value
  * 
  * @return raw value
  */
  public double getValue() {
    return value;
  }

 /**
  * Accessor for the rank value
  * 
  * @return data point rank
  */
  public double getRank() {
    return rank;
  }

 /**
  * Modifier for the rank value
  */
  public void setRank(double d) {
    rank = d;
  }

}