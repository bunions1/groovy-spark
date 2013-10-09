package repl;

import java.util.*;
import spark.api.java.*;
import spark.api.java.function.*;
import scala.Tuple2;
import groovy.lang.Closure;

class PairFlatMapF extends PairFlatMapFunction {
	Closure c = null;
	public PairFlatMapF(Closure c){
		this.c = c;
	}
	public java.lang.Iterable call(Object s){
		return (java.lang.Iterable)c.call(s);
	}
}
