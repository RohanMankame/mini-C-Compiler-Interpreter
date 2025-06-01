public class Program {
    public static void main(String[] args) throws Exception
    {
        /*
          java.io.Reader r = new java.io.StringReader
          (""
          +"func testfunc::num()\n"
          +"begin\n"
          +"    var a::num;\n"
          +"    a := 1;\n"
          +"    return a;\n"
          +"end\n"
          +"func main::num()\n"
          +"begin\n"
          +"    var a::num;\n"
          +"    a := testfunc();"
          +"    return 0;\n"
          +"end\n"
          );

         */


        if(args.length == 0)
        args = new String[]
         // You can edit the file location string below to test the other test examples (make sure dictionary is correct)
        {
        "C:\\Users\\Rohan\\Desktop\\mini-C-Compiler-Interpreter\\samples\\"
        +"succ_01.minc",
        };

        if(args.length <= 0)
            return;
        String minicpath = args[0];
        java.io.Reader r = new java.io.FileReader(minicpath);



        Compiler compiler = new Compiler(r);

        compiler.Parse();


    }
}
