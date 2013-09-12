package repl;

import java.util.*;
import spark.api.java.*;
import spark.api.java.function.*;
import scala.Tuple2;
import groovy.lang.Closure;

public class PairF extends PairFunction {
	Closure c = null;
	public PairF(Closure c){
		this.c = c;
	}
	public Tuple2 call(Object s){
		return (Tuple2)c.call(s);
	}
}
