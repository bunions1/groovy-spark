package repl;

import java.util.*;
import spark.api.java.*;
import spark.api.java.function.*;
import scala.Tuple2;
import groovy.lang.Closure;

public class F2 extends Function2 {
	Closure c = null;
	public F2(Closure c){
		this.c = c;
	}
	public Object call(Object s1, Object s2) {
		return c.call(s1,s2);
	}
}
