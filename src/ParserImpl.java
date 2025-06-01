import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class ParserImpl
{
    public static Boolean _debug = true;

    static class SymbolInfo {
        String type;
        int address;
        ArrayList<ParseTree.Param> params; // For functions

        // Constructor for variables
        public SymbolInfo(String type, int address) {
            this.type = type;
            this.address = address;
            this.params = null;
        }

        // Constructor for functions
        public SymbolInfo(String type, ArrayList<ParseTree.Param> params) {
            this.type = type;
            this.params = params;
            this.address = 0; // Not used for functions in this way
        }
    }


    void Debug(String message)
    {
        if(_debug)
            System.out.println(message);
    }

    // This is for chained symbol table.
    // This includes the global scope only at this moment.
    Env env = new Env(null);
    // this stores the root of parse tree, which will be used to print parse tree and run the parse tree
    ParseTree.Program parsetree_program = null;

    private int nextParamAddr = -1;
    private int nextLocalAddr = 1;

    Object program____decllist(Object s1) throws Exception
    {

        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>)s1;
        parsetree_program = new ParseTree.Program(decllist);
        return parsetree_program;
    }



    Object decllist____decllist_decl(Object s1, Object s2) throws Exception
    {
        ArrayList<ParseTree.FuncDecl> decllist = (ArrayList<ParseTree.FuncDecl>)s1;
        ParseTree.FuncDecl                decl = (ParseTree.FuncDecl           )s2;
        decllist.add(decl);
        return decllist;
    }
    Object decllist____eps() throws Exception
    {
        return new ArrayList<ParseTree.FuncDecl>();
    }
    Object decl____funcdecl(Object s1) throws Exception
    {
        return s1;
    }
    Object primtype____NUM(Object s1) throws Exception
    {
        ParseTree.TypeSpec typespec = new ParseTree.TypeSpec("num");
        return typespec;
    }
    Object typespec____primtype(Object s1)
    {
        ParseTree.TypeSpec primtype = (ParseTree.TypeSpec)s1;
        return primtype;
    }

    Object typespec____primtype_LBRACKET_RBRACKET(Object s1,Object s2,Object s3)
    {
        ParseTree.TypeSpec primtype;
        primtype = (ParseTree.TypeSpec) s1;
        primtype.typename += "[]";
        return primtype;
    }



    Object fundecl____FUNC_IDENT_TYPEOF_typespec_LPAREN_params_RPAREN_BEGIN_localdecls_10X_stmtlist_END(Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8, Object s9) throws Exception
    {
        Token id = (Token) s2;
        ParseTree.TypeSpec type = (ParseTree.TypeSpec) s4;
        ArrayList<ParseTree.Param> params = (ArrayList<ParseTree.Param>) s6;

        if (env.IsInCurrentScope(id.lexeme)) {
            throw new Exception("[Error] Identifier " + id.lexeme + " is already defined.");
        }

        // Add function to the *outer* scope
        env.Put(id.lexeme, new SymbolInfo(type.typename, params));

        // --- Start of Function Body Scope ---
        env = new Env(env);

        // Reset address counters for the new function scope
        nextParamAddr = -1;
        nextLocalAddr = 1;

        // Add parameters to the new (inner) scope with their addresses
        if (params != null) {
            for (ParseTree.Param p : params) {
                if (env.IsInCurrentScope(p.ident)) {
                    throw new Exception("[Error] Identifier " + p.ident + " is already defined.");
                }
                p.reladdr = nextParamAddr;
                env.Put(p.ident, new SymbolInfo(p.typespec.typename, nextParamAddr));
                nextParamAddr--;
            }
        }

        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>)s9;
        ParseTree.FuncDecl funcDec = new ParseTree.FuncDecl(id.lexeme, type, params, localdecls, null);
        return funcDec;
    }

    Object fundecl____FUNC_IDENT_TYPEOF_typespec_LPAREN_params_RPAREN_BEGIN_localdecls_X10_stmtlist_END(Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8, Object s9, Object s10, Object s11, Object s12) throws Exception
    {
        ParseTree.FuncDecl funcdecl = (ParseTree.FuncDecl) s10;
        ArrayList<ParseTree.Stmt> stmtlist = (ArrayList<ParseTree.Stmt>) s11;

        funcdecl.stmtlist = stmtlist;

        // --- End of Function Body Scope ---
        // Restore the outer environment
        env = env.prev;

        return funcdecl;
    }

    Object params____eps() throws Exception
    {
        return new ArrayList<ParseTree.Param>();
    }

    Object stmtlist____stmtlist_stmt(Object s1, Object s2) throws Exception
    {
        ArrayList<ParseTree.Stmt> stmtlist = (ArrayList<ParseTree.Stmt>)s1;
        ParseTree.Stmt            stmt     = (ParseTree.Stmt           )s2;
        stmtlist.add(stmt);
        return stmtlist;
    }
    Object stmtlist____eps() throws Exception
    {
        return new ArrayList<ParseTree.Stmt>();
    }

    Object stmt____assignstmt  (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.AssignStmt);
        return s1;
    }
    Object stmt____returnstmt  (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.ReturnStmt);
        return s1;
    }

    Object stmt____ifstmt  (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.PrintStmt);
        return s1;
    }

    Object stmt____printstmt  (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.PrintStmt);
        return s1;
    }

    Object stmt____whilestmt  (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.PrintStmt);
        return s1;
    }

    Object stmt____compoundstmt  (Object s1) throws Exception
    {
        assert(s1 instanceof ParseTree.PrintStmt);
        return s1;
    }

    Object assignstmt____IDENT_LBRACKET_expr_RBRACKET_ASSIGN_expr_SEMI  (Object s1,Object s2,Object s3,Object s4,Object s5,Object s6,Object s7) throws Exception
    {
        Token token = (Token) s1;    // The array identifier
        ParseTree.Expr expr1 = (ParseTree.Expr) s3; // The index
        ParseTree.Expr expr2 = (ParseTree.Expr) s6; // The value to assign

        Object symbol = env.Get(token.lexeme);
        if (symbol == null || !(symbol instanceof SymbolInfo)) {
            throw new Exception("[Error] Array "+token.lexeme+" is not defined.");
        }

        SymbolInfo info = (SymbolInfo) symbol;
        if (!info.type.endsWith("[]")) {
            throw new Exception("[Error] Identifier "+token.lexeme+" should be array variable.");
        }

        if (!expr1.info.Type.equals("num")) {
            throw new Exception("[Error] Array index must be num value.");
        }

        String elementType = info.type.replace("[]", "");
        if (!elementType.equals(expr2.info.Type)) {
            throw new Exception("[Error] Element of array "+token.lexeme+" should have "+ elementType +" value, instead of "+ expr2.info.Type +" value.");
        }

        ParseTree.AssignStmtForArray assignStmt = new ParseTree.AssignStmtForArray(token.lexeme, expr1, expr2);
        assignStmt.ident_reladdr = info.address; // Set the correct address
        return assignStmt;
    }

    Object printstmt____PRINT_expr_SEMI  (Object s1,Object s2,Object s3) throws Exception
    {
        ParseTree.Expr expr = (ParseTree.Expr) s2;
        return new ParseTree.PrintStmt(expr);
    }


    Object ifstmt____IF_expr_THEN_stmtlist_ELSE_stmtlist_END  (Object s1,Object s2,Object s3,Object s4,Object s5,Object s6,Object s7) throws Exception
    {
        //System.out.println("IN IF stmt");
        ParseTree.Expr expr = (ParseTree.Expr) s2;
        ArrayList<ParseTree.Stmt> thenStmtList = (ArrayList<ParseTree.Stmt>) s4;
        ArrayList<ParseTree.Stmt> elseStmtList = (ArrayList<ParseTree.Stmt>) s6;
        if (expr.info.Type.equals("bool") ) {
            return new ParseTree.IfStmt(expr, thenStmtList, elseStmtList);
        }else {
            throw new Exception("[Error] "+"Condition of if or while statement should be bool value.");
        }
    }


    Object whilestmt____WHILE_expr_BEGIN_stmtlist_END  (Object s1,Object s2,Object s3,Object s4,Object s5) throws Exception
    {
        System.out.println("3");
        ParseTree.Expr expr = (ParseTree.Expr) s2;
        ArrayList<ParseTree.Stmt> stmtList = (ArrayList<ParseTree.Stmt>) s4;
        if (expr.info.Type.equals("bool") ) {
            return new ParseTree.WhileStmt(expr, stmtList);
        }else {
            throw new Exception("[Error] "+"Condition of if or while statement should be bool value.");
        }
        //return new ParseTree.WhileStmt(expr, stmtList);
    }

    Object compoundstmt____BEGIN_localdecls_stmtlist_END(Object s1,Object s2,Object s3,Object s4) throws Exception
    {
        // Create a new scope for the compound statement
        env = new Env(env);

        ArrayList<ParseTree.LocalDecl> localDecls = (ArrayList<ParseTree.LocalDecl>) s2;
        ArrayList<ParseTree.Stmt> stmtList = (ArrayList<ParseTree.Stmt>) s3;

        ParseTree.CompoundStmt compoundStmt = new ParseTree.CompoundStmt(localDecls, stmtList);

        // Restore the previous scope after leaving the block
        env = env.prev;

        return compoundStmt;
    }

    Object args____arglist  (Object s1) throws Exception
    {
        return s1;
    }

    Object arglist____arglist_COMMA_expr  (Object s1,Object s2,Object s3) throws Exception
    {
        ArrayList<ParseTree.Arg> List = (ArrayList<ParseTree.Arg>) s1;
        ParseTree.Expr expr = (ParseTree.Expr) s3;
        List.add(new ParseTree.Arg(expr));
        return List;
    }

    Object arglist____expr  (Object s1) throws Exception
    {
        ArrayList<ParseTree.Arg> List = new ArrayList<>();
        ParseTree.Expr expr = (ParseTree.Expr) s1;
        List.add(new ParseTree.Arg(expr));
        return List;
    }
/////////////////////////////////////////////////////////////////////////

    Object expr____expr_AND_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;

        if (expr1.info.Type.equals("bool") && expr2.info.Type.equals("bool")) {
            ParseTree.ExprAnd BoolExpr = new ParseTree.ExprAnd(expr1,expr2);
            BoolExpr.info.Type = "bool";
            return BoolExpr;
            //return new ParseTree.ExprAnd(expr1,expr2);
        }
        else {
            throw new Exception("[Error] "+"Binary operation and cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }

    }

    Object expr____expr_SUB_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;



        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprSub NumExpr = new ParseTree.ExprSub(expr1,expr2);
            NumExpr.info.Type = "num";
            return NumExpr;
            //return new ParseTree.ExprSub(expr1,expr2);
        }
        else {
            throw new Exception("[Error] "+"Binary operation - cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }
    }
//////////////////////////////////////////////////////////////////////////////
    Object expr____expr_MUL_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;



        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprMul NumExpr = new ParseTree.ExprMul(expr1,expr2);
            NumExpr.info.Type = "num";
            return NumExpr;
            //return new ParseTree.ExprMul(expr1,expr2);
        }
        else {
            throw new Exception("[Error] Binary operation * cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }
        //return new ParseTree.ExprMul(expr1,expr2);


    }

    Object expr____expr_DIV_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;

        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprDiv NumExpr = new ParseTree.ExprDiv(expr1,expr2);
            NumExpr.info.Type = "num";
            return NumExpr;

        }
        else {
            throw new Exception("[Error] Binary operation / cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }


    }

    Object expr____expr_MOD_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;

        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprMod NumExpr = new ParseTree.ExprMod(expr1,expr2);
            NumExpr.info.Type = "num";
            return NumExpr;
            //return new ParseTree.ExprMod(expr1,expr2);
        }
        else {
            throw new Exception("[Error] Binary operation % cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }

    }

    Object expr____expr_NE_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;

        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num"))
        {
            ParseTree.ExprNe BoolExpr = new ParseTree.ExprNe(expr1,expr2);
            BoolExpr.info.Type = "bool";
            return BoolExpr;
            //return new ParseTree.ExprNe(expr1,expr2);
        }
        else
        {
            throw new Exception("[Error] Binary operation <> cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }


    }

    Object expr____expr_LE_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;

        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprLe BoolExpr = new ParseTree.ExprLe(expr1,expr2);
            BoolExpr.info.Type = "bool";
            return BoolExpr;
            //return new ParseTree.ExprLe(expr1,expr2);
        }
        else {
            throw new Exception("[Error] Binary operation <= cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }

    }

    Object expr____expr_LT_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;

        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprLt BoolExpr = new ParseTree.ExprLt(expr1,expr2);
            BoolExpr.info.Type = "bool";
            return BoolExpr;
            //return new ParseTree.ExprLt(expr1,expr2);
        }
        else {
            throw new Exception("[Error] Binary operation < cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }

    }

    Object expr____expr_GE_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;

        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprGe BoolExpr = new ParseTree.ExprGe(expr1,expr2);
            BoolExpr.info.Type = "bool";
            return BoolExpr;
            //return new ParseTree.ExprGe(expr1,expr2);
        }
        else {
            throw new Exception("[Error] Binary operation >= cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }
    }

    Object expr____expr_GT_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;


        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprGt BoolExpr = new ParseTree.ExprGt(expr1,expr2);
            BoolExpr.info.Type = "bool";
            return BoolExpr;
            //return new ParseTree.ExprGt(expr1,expr2);
        }
        else {
            throw new Exception("[Error] Binary operation > cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }
    }



    Object expr____expr_OR_expr  (Object s1,Object s2,Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;


        if (expr1.info.Type.equals("bool") && expr2.info.Type.equals("bool")) {
            ParseTree.ExprOr BoolExpr = new ParseTree.ExprOr(expr1,expr2);
            BoolExpr.info.Type = "bool";
            return BoolExpr;
            //return new ParseTree.ExprOr(expr1,expr2);
        }
        else {
            throw new Exception("[Error] Binary operation or cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }

        //return new ParseTree.ExprOr(expr1,expr2);
    }



    Object expr____NOT_expr  (Object s1,Object s2) throws Exception
    {
        ///CHECK IF CORRECT NOT SURE

        ParseTree.Expr expr = (ParseTree.Expr) s2;
        if (expr.info.Type == "bool") {
            ParseTree.ExprNot BoolExpr = new ParseTree.ExprNot(expr);
            BoolExpr.info.Type = "bool";
            return BoolExpr;
        }
        else {
            throw new Exception("[Error] Unary operation not cannot be used with "+ expr.info.Type +" value.");
        }

    }


    Object expr____expr_ADD_expr(Object s1, Object s2, Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;

        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprAdd NumExpr = new ParseTree.ExprAdd(expr1,expr2);
            NumExpr.info.Type = "num";
            return NumExpr;

        }
        else {
            throw new Exception("[Error] Binary operation + cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }

        //a.info.Type = "num";

        //return new ParseTree.ExprAdd(expr1,expr2);
    }


    Object expr____expr_EQ_expr(Object s1, Object s2, Object s3) throws Exception
    {

        ParseTree.Expr expr1 = (ParseTree.Expr)s1;
        Token          oper  = (Token         )s2;
        ParseTree.Expr expr2 = (ParseTree.Expr)s3;

        if (expr1.info.Type.equals("num") && expr2.info.Type.equals("num")) {
            ParseTree.ExprEq BoolExpr = new ParseTree.ExprEq(expr1,expr2);
            BoolExpr.info.Type = "bool";
            return BoolExpr;
        }
        else {
            throw new Exception("[Error] Binary operation = cannot be used with " + expr1.info.Type +" and "+ expr2.info.Type + " values.");
        }

        //ParseTree.ExprEq a = new ParseTree.ExprEq(expr1,expr2);
        //a.info.Type = "bool";

        //return new ParseTree.ExprEq(expr1,expr2);
    }

    Object expr____NEW_primtype_LBRACKET_expr_RBRACKET  (Object s1,Object s2,Object s3,Object s4,Object s5) throws Exception
    {
        ParseTree.TypeSpec typeSpec = (ParseTree.TypeSpec) s2;
        ParseTree.Expr expr = (ParseTree.Expr) s4;
        ParseTree.ExprNewArray ExprArr = new ParseTree.ExprNewArray(typeSpec, expr);
        ExprArr.info.Type = typeSpec.typename + "[]";

        return ExprArr;
    }


    Object expr____IDENT_LBRACKET_expr_RBRACKET  (Object s1,Object s2,Object s3,Object s4) throws Exception
    {
        Token token = (Token) s1;
        ParseTree.Expr expr = (ParseTree.Expr) s3;

        Object symbol = env.Get(token.lexeme);
        if(symbol == null || !(symbol instanceof SymbolInfo)){
            throw new Exception("[Error] Array "+ token.lexeme +" is not defined.");
        }

        SymbolInfo info = (SymbolInfo) symbol;
        if(!info.type.endsWith("[]")){
            throw new Exception("[Error] Identifier "+ token.lexeme +" should be array variable.");
        }

        if(!expr.info.Type.equals("num")){
            throw new Exception("[Error] Array index must be num value.");
        }

        String elementType = info.type.replace("[]" , "");
        ParseTree.ExprArrayElem exprArrayElem = new ParseTree.ExprArrayElem(token.lexeme, expr);
        exprArrayElem.info.Type = elementType; // The type of the expression is the element's type
        exprArrayElem.reladdr = info.address;  // Set the correct address

        return exprArrayElem;
    }

    Object expr____IDNT_DOT_SIZE  (Object s1,Object s2,Object s3) throws Exception
    {
        Token token = (Token) s1;
        Object symbol = env.Get(token.lexeme);

        if(symbol == null || !(symbol instanceof SymbolInfo)){
            throw new Exception("[Error] Array "+ token.lexeme +" is not defined.");
        }

        SymbolInfo info = (SymbolInfo) symbol;
        if(!info.type.endsWith("[]")){
            throw new Exception("[Error] Identifier "+ token.lexeme +" should be array variable.");
        }

        ParseTree.ExprArraySize exprArraySize = new ParseTree.ExprArraySize(token.lexeme);
        exprArraySize.info.Type = "num"; // The .size property always returns a number
        exprArraySize.reladdr = info.address; // Set the correct address
        return exprArraySize;
    }


    Object expr____LPAREN_expr_RPAREN(Object s1, Object s2, Object s3) throws Exception
    {

        Token          lparen = (Token         )s1;
        ParseTree.Expr expr   = (ParseTree.Expr)s2;
        Token          rparen = (Token         )s3;


        ParseTree.ExprParen a = new ParseTree.ExprParen(expr);
        a.info.Type = expr.info.Type;
        return a;
    }

    Object expr____IDENT_LPAREN_args_RPAREN(Object s1, Object s2, Object s3, Object s4) throws Exception
    {

        Token                    id   = (Token                   )s1;
        ArrayList<ParseTree.Arg> args = (ArrayList<ParseTree.Arg>)s3;

        //System.out.println("IMMMMMM HERE");

        return new ParseTree.ExprFuncCall(id.lexeme, args);



    }


    Object expr____IDENT(Object s1) throws Exception
    {
        Token id = (Token)s1;
        Object symbol = env.Get(id.lexeme);

        if (symbol == null || !(symbol instanceof SymbolInfo)) {
            throw new Exception("[Error] Variable "+ id.lexeme +" is not defined.");
        }

        SymbolInfo info = (SymbolInfo) symbol;
        if(info.params != null) {
            throw new Exception("[Error] Identifier "+id.lexeme+" should be non-function type.");
        }

        ParseTree.ExprIdent exprIdent = new ParseTree.ExprIdent(id.lexeme);
        exprIdent.info.Type = info.type;
        exprIdent.reladdr = info.address; // Use the addy from ST
        return exprIdent;
    }



    Object expr____NUMLIT(Object s1) throws Exception
    {
        Token token = (Token) s1;
        double value = Double.parseDouble(token.lexeme);
        ParseTree.ExprNumLit number = new ParseTree.ExprNumLit(value);
        number.info.Type = "num";
        return number;
    }

    Object expr____BOOLLIT  (Object s1) throws Exception
    {
        Token token = (Token) s1;
        boolean value = Boolean.parseBoolean(token.lexeme);
        ParseTree.ExprBoolLit bool = new ParseTree.ExprBoolLit(value);
        bool.info.Type = "bool";
        return bool;
    }




    Object params____paramlist  (Object s1) throws Exception
    {
        return s1;
    }

    Object paramlist____param  (Object s1) throws Exception
    {
        List<ParseTree.Param> v1;
        v1 = new ArrayList<>();
        v1.add((ParseTree.Param) s1);
        return v1;
    }

    Object paramlist____paramlist_COMMA_param  (Object s1,Object s2,Object s3) throws Exception
    {
        ArrayList<ParseTree.Param> v1;
        v1 = (ArrayList<ParseTree.Param>) s1;
        v1.add((ParseTree.Param) s3);
        return v1;
    }

    Object param____IDENT_TYPEOF_typespec  (Object s1,Object s2,Object s3) throws Exception
    {
        return new ParseTree.Param(((Token)s1).lexeme, (ParseTree.TypeSpec) s3);
    }

    Object param_list____primtype_LBRACKET_RBRACKET  (Object s1,Object s2,Object s3) throws Exception
    {
        assert(s1 instanceof ParseTree.PrintStmt);
        return s1;
    }

    Object primtype____BOOL  (Object s1) throws Exception
    {
        ParseTree.TypeSpec typespec = new ParseTree.TypeSpec("bool");
        return typespec;
    }



    Object assignstmt____IDENT_ASSIGN_expr_SEMI(Object s1, Object s2, Object s3, Object s4) throws Exception
    {
        Token id = (Token)s1;
        ParseTree.Expr expr = (ParseTree.Expr)s3;

        Object symbol = env.Get(id.lexeme);
        if (symbol == null || !(symbol instanceof SymbolInfo)) {
            throw new Exception("[Error] Variable "+id.lexeme+" is not defined.");
        }

        SymbolInfo info = (SymbolInfo) symbol;

        if(info.params != null) {
            throw new Exception("[Error] Identifier "+id.lexeme+" should be non-function type.");
        }

        if(!info.type.equals(expr.info.Type)) {
            throw new Exception("[Error] Variable "+ id.lexeme +" should have "+ info.type +" value, instead of "+ expr.info.Type +" value.");
        }

        ParseTree.AssignStmt stmt = new ParseTree.AssignStmt(id.lexeme, expr);
        stmt.ident_reladdr = info.address;
        return stmt;
    }



    Object returnstmt____RETURN_expr_SEMI(Object s1, Object s2, Object s3) throws Exception
    {

        ParseTree.Expr expr = (ParseTree.Expr)s2;
        return new ParseTree.ReturnStmt(expr);
    }



    Object localdecls____localdecls_localdecl(Object s1, Object s2)
    {
        ArrayList<ParseTree.LocalDecl> localdecls = (ArrayList<ParseTree.LocalDecl>)s1;
        ParseTree.LocalDecl            localdecl  = (ParseTree.LocalDecl           )s2;
        localdecls.add(localdecl);
        return localdecls;
    }
    Object localdecls____eps() throws Exception
    {
        return new ArrayList<ParseTree.LocalDecl>();
    }


    Object localdecl____VAR_IDENT_TYPEOF_typespec_SEMI(Object s1, Object s2, Object s3, Object s4, Object s5) throws Exception {
        Token id = (Token)s2;
        ParseTree.TypeSpec typespec = (ParseTree.TypeSpec)s4;

        if(env.IsInCurrentScope(id.lexeme))
        {
            throw new Exception("[Error]" + "Identifier " + id.lexeme + " is already defined.");
        }

        // Assign the next available address and store the symbol info.
        int addr = nextLocalAddr++;
        env.Put(id.lexeme, new SymbolInfo(typespec.typename, addr));

        ParseTree.LocalDecl localdecl = new ParseTree.LocalDecl(id.lexeme, typespec);
        localdecl.reladdr = addr;
        return localdecl;
    }

    Object args____eps() throws Exception
    {
        return new ArrayList<ParseTree.Expr>();
    }




}
