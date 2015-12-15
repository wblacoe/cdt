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

import java.util.Arrays;
import java.util.Vector;

/**
 * Class for calculating Spearman's Rank Correlation between two vectors.
 * <p>
 * For more information, go <a href="http://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient">here</a>.
 * <p>
 * TODO: More calculation checks for consistency.
 * 
 * @author weale
 * @version 0.9
 *
 */
public class Spearman {
        
        /**
         * Calculate the Spearman's Rank Correlation Coefficient of X and Y
         * 
         * @param X original human relatedness values
         * @param Y metric relatedness values 
         * @return
         */
        public static double GetCorrelation(Vector<Double> X, Vector<Double> Y) {
                
                // Set up the Ranking elements
                Ranking[] XList = new Ranking[X.size()];
                Ranking[] YList = new Ranking[Y.size()];                
                for(int i=0; i<XList.length; i++) {
                        XList[i] = new Ranking(i,X.elementAt(i));
                        YList[i] = new Ranking(i,Y.elementAt(i));
                }
                
                // Sort the Ranking lists
                Arrays.sort(XList, new RankingComparator());
                Arrays.sort(YList, new RankingComparator());
                
                // Set the rank for the new lists
                for(int i=0; i<XList.length; i++) {
                        XList[i].setRank(i+1);
                        YList[i].setRank(i+1);
                }
                
                // Check for ties and modify rankings as needed
                SetRank(XList);
                SetRank(YList);
                
                double d2 = 0.0;
                for(int i=0; i<XList.length; i++) {
                        Ranking r = XList[i];
                        
                        boolean found = false;
                        for(int j=0; j<YList.length && !found; j++) {
                                Ranking r2 = YList[j];
                                if(r2.getID() == r.getID()) {
                                        double d = r.getRank() - r2.getRank();
                                        //System.out.println(r.getRank() + "\t" + r2.getRank());
                                        found = true;
                                        d2 += (d*d);
                                }//end: if()
                        }//end: for(j)
                }//end: for(i)

                double n = X.size();
                double den = n * ((n * n) -1);
                double num = 6.0 * d2;
                
                double rho = 1- (num/den);
                return rho;
        }//end: GetCorrelation(X,Y)
        
        /**
         * 
         * @param List
         */
        private static void SetRank(Ranking[] List) {
                
                // Set finalized flag for each element in the list
                boolean[] finalized = new boolean[List.length];
                for(int i=0; i<finalized.length; i++) {
                        finalized[i] = false;
                }//end: for(i)
                
                // For each element in the list
                int index=0;
                while(index < List.length) {
                        if(!finalized[index]) {
                        
                                // Get current ranking & val
                                double val = List[index].getValue();
                                double rank = List[index].getRank();
                                int num = 1;
        
                                while(index+num != List.length && List[index+num].getValue() == val) {
                                        rank += List[index+num].getRank();
                                        num++;
                                }
                                
                                if(num > 1 && index+num == List.length) {
                                        num--;
                                }
                                
                                if(num > 1) {
                                        double newRank = rank / num;
                                        for(int j=0; j<num; j++) {
                                                List[index+j].setRank(newRank);
                                        }//end: for(j)          
                                }//end: if(num)
                                
                        }//end: if(!finalized)
                        index = index + 1;
                }//end: while(index)
        }//end: SetRank(Ranking[])
        
        /**
         * @param args
         */
        public static void main(String[] args) {
                Vector<Double> X = new Vector<Double>();
                Vector<Double> Y = new Vector<Double>();
//              X.add(106.0);
//              X.add(86.0);
//              X.add(100.0);
//              X.add(101.0);
//              X.add(99.0);
//              X.add(103.0);
//              X.add(97.0);
//              X.add(113.0);
//              X.add(112.0);
//              X.add(110.0);
//
//              Y.add(7.0);
//              Y.add(0.0);
//              Y.add(27.0);
//              Y.add(50.0);
//              Y.add(28.0);
//              Y.add(29.0);
//              Y.add(20.0);
//              Y.add(12.0);
//              Y.add(6.0);
//              Y.add(17.0);
                
                X.add(1.2);
                X.add(1.3);
                X.add(1.6);
                X.add(1.3);
                X.add(1.0);
                X.add(2.0);
                X.add(1.7);
                X.add(1.6);
                
                Y.add(0.0);
                Y.add(1.3);
                Y.add(0.0);
                Y.add(1.4);
                Y.add(1.6);
                Y.add(2.0);
                Y.add(1.4);
                Y.add(1.4);

                System.out.println(GetCorrelation(X, Y));
        }
}