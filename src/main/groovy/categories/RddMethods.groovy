package categories

import spark.api.java.*
import spark.api.java.function.*;
import scala.Tuple2;
import repl.*;



class RddMethods{

    static def each(spark.api.java.JavaRDDLike r, Closure closure){
       return r.foreach(new VF(closure))
    }
	
    static def collect(spark.api.java.JavaRDD r, Closure closure){
		return r.map(new F(closure))
    }

    static def collect(spark.api.java.JavaPairRDD r, Closure closure){
		return r.map(new PairF(closure))
    }

    static def collectEntries(spark.api.java.JavaRDD r, Closure closure){
		def wrapped  = { input ->
			def result = closure(input)
			return new Tuple2(result[0], result[1])
		}
		return r.map(new PairF(wrapped))
    }

    static def collectEntries(spark.api.java.JavaPairRDD r, Closure closure){
		def wrapped  = { input->
			def result = closure(input._1, input._2)
			return new Tuple2(result[0], result[1])
		}
		return r.map(new PairF(wrapped))
    }


	
	static def collectMany(spark.api.java.JavaRDDLike r, Closure closure){

		return r.flatMap(new FlatMapF(closure))
	}

	static def collectManyEntries(spark.api.java.JavaRDDLike r, Closure closure){
		def wrapped  = { input ->
			def result = closure(input)
			return result.collect{k, v ->
				new Tuple2(k, v)
			}
		}
		return r.flatMap(new PairFlatMapF(wrapped))
	}
}