package repl;

import java.util.*;
import spark.api.java.*;
import spark.api.java.function.*;
import scala.Tuple2;
import groovy.lang.Closure;

public class F extends Function {
	Closure c = null;
	public F(Closure c){
		this.c = c;
	}
	public Object call(Object s){
		return c.call(s);
	}
}
