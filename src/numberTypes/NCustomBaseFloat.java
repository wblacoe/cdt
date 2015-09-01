package numberTypes;

import cdt.Helper;
import space.TensorSpace;
import space.dep.DepNeighbourhoodSpace;

/**
 * number = (isPositive ? 1 : -1) * factor * sqrt(dimensionality)^exponent, where exponent is usually negative
 * @author wblacoe
 */
public class NCustomBaseFloat extends NNumber {

	private boolean isPositive;
	private float factor; //should always be positive
	private int exponent; //is typically negative

	//standard value is 0
	public NCustomBaseFloat(boolean isPositive, float factor, int exponent){
		//make sure factor is positive
		if(factor < 0){
			isPositive = !isPositive;
			factor = -factor;
		}
		this.isPositive = isPositive;
		
		//make sure factor is within base range
        if(factor == 0 || (factor >= 1 && factor < TensorSpace.getDimensionalitySqrt())){
            this.factor = factor;
            this.exponent = exponent;
        }else{
            double factorLog = Math.log(factor) / TensorSpace.getDimensionalitySqrtLog();
            //System.out.println("log_" + Helper.getDimensionalitySqrt() + " " + factor + " = " + factorLog); //DEBUG
            int carry;
            if(factorLog >= 0){
                carry = (int) factorLog;
            }else{
                carry = (int) (factorLog - 1);
            }
            
            if(carry != 0){
                this.factor = (float) Math.pow(TensorSpace.getDimensionalitySqrt(), factorLog - carry);
                //System.out.println("factor_inRange = " + Helper.getDimensionalitySqrt() + "^(" + factorLog + " - " + carry + ") = " + this.factor); //DEBUG
            }
            this.exponent = exponent + carry;
            //System.out.println(this); //DEBUG
        }
	}
    public NCustomBaseFloat(){
		//this(true, 0, 0);
        isPositive = true;
        factor = 0;
        exponent = 0;
    }
    public NCustomBaseFloat(int n){
		this(true, n, 0);
    }
    public NCustomBaseFloat(float n){
        this(true, n, 0);
    }
    public NCustomBaseFloat(double n){
        this(true, (float) n, 0);
    }
    public NCustomBaseFloat(String nString){
        this(Float.parseFloat(nString));
    }

    
	public float getFactor(){
		return factor;
	}
	public int getExponent(){
		return exponent;
	}
    
    @Override
    public int logBaseDimensionalitySqrtToInt(){
        return exponent;
    }
    
    @Override
    public boolean isZero(){
        return factor == 0;
    }
    @Override
	public boolean isPositive(){
		return isPositive;
	}
    @Override
    public boolean isNegative(){
        return !isPositive;
    }
	
    //if both numbers have the same exponent add them, otherwise ignore the number with the smaller exponent
    public NNumber addQuick(NNumber n) {
        NCustomBaseFloat r = (NCustomBaseFloat) n;
		
        NCustomBaseFloat sum;
        
		//if both numbers have the same exponent
		if(exponent == r.getExponent()){
            float sumOfFactors = ((isPositive() ? 1 : -1) * factor) + ((r.isPositive() ? 1 : -1) * r.getFactor());
            boolean isSumPositive = (sumOfFactors >= 0);
            if(!isSumPositive) sumOfFactors *= -1;
			sum = new NCustomBaseFloat(isSumPositive, sumOfFactors, exponent);
            sum.carry();
			
		//if exponents differ
		}else{
            //keep only the number with the higher exponent
			if(exponent < r.getExponent()){
				sum = r;
			}else{
				sum = this;
			}
		}
        
        return sum;
    }

    //raise the number with the higher absolute value to the exponent of the other one, then add factors
    public NNumber addSlow(NNumber n) {
		NCustomBaseFloat r = (NCustomBaseFloat) n;
		
        NCustomBaseFloat sum;
        
		//if both numbers have the same exponent
		if(exponent == r.getExponent()){
            float sumOfFactors = ((isPositive() ? 1 : -1) * factor) + ((r.isPositive() ? 1 : -1) * r.getFactor());
            boolean isSumPositive = (sumOfFactors >= 0);
            if(!isSumPositive) sumOfFactors *= -1;
			sum = new NCustomBaseFloat(isSumPositive, sumOfFactors, exponent);
            sum.carry();
			
		//if exponents differ
		}else{
			//sort numbers by exponent
			NCustomBaseFloat smaller, larger;
			if(exponent < r.getExponent()){
				smaller = this;
				larger = r;
			}else{
				smaller = r;
				larger = this;
			}
			//add the smaller number to the bigger in the order of the bigger number
			float smallerFactorInRelativeOrderOfBiggerNumber = smaller.getFactor() * ((float) Math.pow(TensorSpace.getDimensionality(), (smaller.getExponent() - larger.getExponent()) * 0.5));
            float sumOfFactors = ((larger.isPositive() ? 1 : -1) * larger.getFactor()) + ((smaller.isPositive() ? 1 : -1) * smallerFactorInRelativeOrderOfBiggerNumber);
            boolean isSumPositive = (sumOfFactors >= 0);
            if(!isSumPositive) sumOfFactors *= -1;
			sum = new NCustomBaseFloat(isSumPositive, sumOfFactors, larger.getExponent());
            sum.carry();
		}
        
        return sum;
    }
    
    @Override
    public NNumber add(NNumber n) {
		NCustomBaseFloat r = (NCustomBaseFloat) n;
        
        //check for zeros
        //if(isZero()){
            //return r;
        //}else if(r.isZero()){
            //return this;
        //}else{
            if(TensorSpace.getSpeedOfNCustomBaseFloatAddition() == Helper.QUICK){
                return addQuick(r);
            }else{
                return addSlow(r);
            }
        //}
    }
    

	//if factor > dimensionality, adjust factor and exonent so that again 0 <= factor < dimensionality
	public void carry(){
        if(factor != 0){
            if(factor > TensorSpace.getDimensionalitySqrt()){
                factor /= TensorSpace.getDimensionalitySqrt();
                exponent++;
            }else if(factor < 1){
                factor *= TensorSpace.getDimensionalitySqrt();
                exponent--;
            }
        }
	}
	
    @Override
    public NNumber multiply(NNumber n) {
        NCustomBaseFloat r = (NCustomBaseFloat) n;
		NCustomBaseFloat product = new NCustomBaseFloat(isPositive == r.isPositive(), factor * r.getFactor(), exponent + r.getExponent());
		product.carry();
		return product;
    }
    
    @Override
    public NNumber multiply(int n){
        NCustomBaseFloat product = new NCustomBaseFloat(n > 0 == isPositive, factor * Math.abs(n), exponent);
		product.carry();
		return product;
    }
    
    @Override
    public NNumber multiply(float n){
        NCustomBaseFloat product = new NCustomBaseFloat(n > 0 == isPositive, factor * Math.abs(n), exponent);
		product.carry();
		return product;
    }
    
    @Override
    public NNumber multiply(double n){
        NCustomBaseFloat product = new NCustomBaseFloat(n > 0 == isPositive, factor * (float) Math.abs(n), exponent);
		product.carry();
		return product;
    }
	
	/*@Override
	public NNumber multiplyByDimensionalitySqrtToThePowerOf(int e){
		return new NCustomBaseFloat(isPositive, factor, exponent + e);
	}
    */
    
	@Override
	public NNumber invert(){
		return new NCustomBaseFloat(!isPositive, factor, exponent);
	}
    
    @Override
    public NNumber reciprocal(){
        NCustomBaseFloat n = new NCustomBaseFloat(isPositive, 1/factor, -exponent);
		n.carry();
		return n;
    }

    @Override
    public NNumber sqrt(){
        if(!isPositive){
            return null;
        }else if(exponent % 2 == 0){
            return new NCustomBaseFloat(true, (float) Math.sqrt(factor), exponent / 2);
        }else{
            float dimensionalitySqrtSqrt = (float) Math.sqrt(TensorSpace.getDimensionalitySqrt());
            NCustomBaseFloat n = new NCustomBaseFloat(true, (float) Math.sqrt(factor) * dimensionalitySqrtSqrt, (exponent - 1) / 2);
            n.carry();
            return n;
        }
    }
    
    @Override
    public NNumber abs(){
        if(isPositive){
            return this;
        }else{
            return invert();
        }
    }
	
	@Override
	public NNumber min(NNumber n){
		if(isPositive){
			if(n.isPositive()){
				if(exponent < ((NCustomBaseFloat) n).getExponent()){
					return getCopy();
				}else if(exponent > ((NCustomBaseFloat) n).getExponent()){
					return n.getCopy();
				}else{
					if(factor < ((NCustomBaseFloat) n).getFactor()){
						return getCopy();
					}else{
						return n.getCopy();
					}
				}
			}else{
				return n.getCopy();
			}
		}else{
			if(n.isPositive()){
				return getCopy();
			}else{
				if(exponent < ((NCustomBaseFloat) n).getExponent()){
					return n.getCopy();
				}else if(exponent > ((NCustomBaseFloat) n).getExponent()){
					return getCopy();
				}else{
					if(factor < ((NCustomBaseFloat) n).getFactor()){
						return n.getCopy();
					}else{
						return getCopy();
					}
				}
			}
		}
	}

	@Override
	public NNumber max(NNumber n){
		if(isPositive){
			if(n.isPositive()){
				if(exponent < ((NCustomBaseFloat) n).getExponent()){
					return n.getCopy();
				}else if(exponent > ((NCustomBaseFloat) n).getExponent()){
					return getCopy();
				}else{
					if(factor < ((NCustomBaseFloat) n).getFactor()){
						return n.getCopy();
					}else{
						return getCopy();
					}
				}
			}else{
				return getCopy();
			}
		}else{
			if(n.isPositive()){
				return n.getCopy();
			}else{
				if(exponent < ((NCustomBaseFloat) n).getExponent()){
					return getCopy();
				}else if(exponent > ((NCustomBaseFloat) n).getExponent()){
					return n.getCopy();
				}else{
					if(factor < ((NCustomBaseFloat) n).getFactor()){
						return getCopy();
					}else{
						return n.getCopy();
					}
				}
			}
		}
	}
	
	@Override
	public NNumber getCopy(){
		NCustomBaseFloat n = new NCustomBaseFloat(isPositive, factor, exponent);
		return n;
	}
	
	@Override
	public double getDoubleValue(){
        if(isZero()){
            return 0.0;
        }else{
            int signum = (isPositive ? 1 : -1);
            if(exponent == 0){
                return signum * factor;
            }else{
                return signum * factor * Math.pow(TensorSpace.getDimensionality(), exponent * 0.5);
            }
        }
	}
	
	@Override
	public boolean isInfinite(){
		return Float.isInfinite(factor) || Float.isInfinite(exponent);
	}
	
	@Override
	public boolean isNaN(){
		return Float.isNaN(factor) || Float.isNaN(exponent);
	}
    
    @Override
    public int compareTo(Object o){
        //if(!(o instanceof NCustomBaseFloat)) throw new IllegalArgumentException("[NCustomBaseFloat] Bad comparison: Wrong type");
        NCustomBaseFloat n = (NCustomBaseFloat) o;
        
        //check for zero factors
        if(isZero() && n.isZero()){
            return 0;
            
        //compare signs
        }else if((isPositive() || isZero()) && (n.isNegative() || n.isZero())){
            return 1;
        }else if((isNegative() || isZero()) && (n.isPositive() || n.isZero())){
            return -1;
        
        //compare exponents
        }else if(exponent > n.getExponent()){
            return 1;
        }else if(exponent < n.getExponent()){
            return -1;
            
        //compare factors
        }else{
            return Float.compare(factor, n.getFactor());
        }
 
    }
	
    @Override
    public String toString(){
		String s = (isPositive ? "+" : "-");
		s += factor + "*" + TensorSpace.getDimensionality() + "^(" + exponent + "/2)";
		return s;
        //return "" + getDoubleValue();
    }
    
    public static void main(String[] args){
        TensorSpace ts = new DepNeighbourhoodSpace();
        TensorSpace.setOrder(10);
		TensorSpace.setDimensionality(300);
		TensorSpace.setNumberType(NNumber.CUSTOM_BASE_FLOAT);

        NNumber.create(0.0041495436f);
        //NNumber.create(100.0041495436f);
        
        System.out.println("" + (-3 % 2));
    }
    
}