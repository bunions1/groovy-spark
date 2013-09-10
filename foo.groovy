import static repl.Repl.*


class A{

    def a_varOne
    def a_varTwo
    def a_varThree

    def baz = {
        def a = 1
        def b = 2
        def c = 2
        def d = 2
        def e = 2
        def f = 2
		repl()
    }




    def foo(String var){
        println("start")




    
        def (bil, ted, fred) = ["1111111111111",2,3]
        def amap = ["1": 1, "2": 2]
        repl()

        def test = "2"

        for(int i = 0; i < 2; ++i){
            println("loop:" + i)
        }


        println(amap)
		repl()

    }

    def auth = {
        println("inauth")
        def holla = 1
		repl()

        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"


    }

    def dummy(a){
    }


    def taz = {
        def outer = "outer"
        def a = { stuff ->
            def inner = "inner"
			repl()
        }
		repl()
        return a
    }




}



def a = new A()
a.foo("astring")
a.baz()
println "la bouch"
a.taz()()
