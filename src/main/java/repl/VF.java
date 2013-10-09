package repl;

import java.util.*;
import spark.api.java.*;
import spark.api.java.function.*;
import scala.Tuple2;
import groovy.lang.Closure;

class VF extends VoidFunction {
	Closure c = null;
	public VF(Closure c){
		this.c = c;
	}
	public void call(Object s){
		c.call(s);
		return;
	}
}
