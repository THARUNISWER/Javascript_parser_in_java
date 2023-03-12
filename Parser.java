import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors; 
import java.util.*;
import java.lang.*;

public class Parser {
	//whole re-formatted code line by line
	static ArrayList<String> reformatted_code = new ArrayList<String>();
	static int len; // total lines in re-formatted code
	static String reformatted_code_new_line="";
	static String line; // iterating through whole code line by line for syntax error checking
	static int l=0; //stores current line number

	static ArrayList<Integer> comments_len= new ArrayList<>();
	static int comment_index=0;// to iterate over comments_len array list
	static boolean multi_lined_comment= false;

	static Stack <Character> bracket = new Stack<Character>();
	// used in '{','}' balancing to store line number of '{'
	//used in '(',')' balancing to store index of '(' in that particular line
	static Stack <Integer> bracketPos = new Stack<Integer>() ;
	static int osb=0, ocb=0; //for bracket balancing
	
	static Stack <Integer> if_check = new Stack<Integer>() ;
	static Stack <Integer> do_while_check = new Stack<Integer>();
	static boolean el_possible= false; //to check whether else/else-if have a corresponding if statement
	static boolean while_possible= true;

	static int tab_num=0; //for printing tab spaces
	static boolean error=false; // for errors

	static String whole_code=""; //entire code is stored here in string format
	static int whole_code_len= whole_code.length(); 
	static int code_itr=0; //itearting whole code character by character

	public static void is_identifier(String stmt, int i){
		boolean is_id=true;
		int n= stmt.length();
		if(stmt.length()>0 && (Character.isLetter(stmt.charAt(0)) || stmt.charAt(0)=='$'|| stmt.charAt(0)=='_')){ // identifier starts with _ or $ or any alphabet
			for(int k=1; k<=n-1;k++){
				if(!(Character.isLetterOrDigit(stmt.charAt(k)) || stmt.charAt(k)=='$'|| stmt.charAt(k)=='_')){
					is_id=false;
				}
			}
		}else{
			is_id=false;
		}
		if(!is_id){
			System.out.println("Error at line number "+(l+1)+" : Invalid Identifier.");
			error=true;
			print_error(line, i-1, tab_num);
		}				
		return ;
	}
	public static void is_value(String stmt, int i){
		boolean is_val=true;
		int n=stmt.length();
		if(Character.isDigit(stmt.charAt(0)) || stmt.charAt(0)=='-'){ // number check
			int dot_cnt=0;
			for(int k=1; k<=n-1;k++){
				if(stmt.charAt(k)=='.' && dot_cnt==0){
					dot_cnt++;
				}
				if(!(Character.isDigit(stmt.charAt(k)) || (stmt.charAt(k)=='.' && dot_cnt==1 && !stmt.endsWith(".")) )){
					is_val= false;
					break;
				}
			}
		}else {
			is_val=false;
		}
		if(!is_val){
			System.out.println("Error at line number "+(l+1)+" : Invalid Value.");
			error=true;
			print_error(line, i-1, tab_num);
		}		
	}
	public static void is_stmt(String line, int i){
		int n= line.length();
		String stmt="";
		while(i<n){
			if(line.charAt(i)==' ') i++;
			//id
			while(i<n){
				if(line.charAt(i)==' '||line.charAt(i)=='='||line.charAt(i)==','||line.charAt(i)==';'){
					if(stmt==""){
						System.out.println("Error at line number "+(l+1)+" : Identifier Expected.");
						print_error(line, i-1, tab_num);
						error=true;
						return;
					}else{
						is_identifier(stmt, i);
						stmt="";
						break;
					}
				}
				stmt+= line.charAt(i);
				i++;
			}
			if(i<n && line.charAt(i)==' '){
				i++;
				if(i<n && line.charAt(i)!='='&&line.charAt(i)!=','&&line.charAt(i)!=';'){
					System.out.println("Error at line number "+(l+1)+" : Expected ',' or ';' after identifier.");
					print_error(line, i-1, tab_num);
					error=true;
					return;
				}
			}
			if(i<n && line.charAt(i)=='='){
				i++;
				if(i<n && line.charAt(i)==' ') i++;
				// id or val after '='
				while(i<n){
					if(i<n && line.charAt(i)==' '||line.charAt(i)==','||line.charAt(i)==';'){
						if(stmt==""){
							System.out.println("Error at line number "+(l+1)+" : Identifier or value is expected.");
							print_error(line, i-1, tab_num);
							error=true;
							return;
						}else{
							if(Character.isLetter(stmt.charAt(0)) ||stmt.charAt(0)=='$'|| stmt.charAt(0)=='_'){
								is_identifier(stmt, i);
								stmt="";
							}else if(Character.isDigit(stmt.charAt(0)) || stmt.charAt(0)=='-'){ // number check
								
								is_value(stmt, i);
								stmt="";
							}else if(stmt.charAt(0)=='\"'){
								if(!stmt.endsWith("\"")){
									System.out.println("Error at line number "+(l+1)+" : Invalid String declaration.");
								}
								stmt="";
							}else if(stmt.charAt(0)=='\''){
								if(!stmt.endsWith("\'")){
									System.out.println("Error at line number "+(l+1)+" : Invalid Character declaration.");
								}
								stmt="";
							}
							break;
						}
					}
					stmt+= line.charAt(i);
					i++;
				}
				if(i<n && line.charAt(i)==' '){
					i++;
					if(i<n && line.charAt(i)!='='&&line.charAt(i)!=','&&line.charAt(i)!=';'){
						System.out.println("Error at line number "+(l+1)+" : Expected ',' or ';' after identifier.");
						print_error(line, i-1, tab_num);
						error=true;
						return;
					}
				}										
			}
			if(i<n && line.charAt(i)==','){
				i++;
				continue;
			}
			if(i<n && line.charAt(i)==';'){
				i++;
				return;
			}
		}
		if(line.charAt(n-1)!=';'){
			System.out.println("Error at line number "+(l+1)+" : Expected ';' after identifier.");
			print_error(line, n-1, tab_num);
			error=true;
			return ;				
		}
	}
	public static void var_check(){
		int i=3;
		int n=line.length();
		while(i<n){
			if(line.substring(i-3,i).equals("var")) break;
			i++;
		}
		if(line.charAt(i)!=' '){
			return;
		}else{
			i++;
			is_stmt(line, i);
		}
	}
	public static void if_else_if_check(){
		int i = 0, n=line.length();
		osb=0;
		bracketPos.clear();


		if(line.contains("else if")){
			i=7;
			while(i<n){
				if(line.substring(i-7,i).equals("else if")) break;
				i++;
			}			
			if(!el_possible){
				System.out.println("Error at line number " + (l+1) + " : This ELSE IF statement doesn't correspond to any IF statement." );
				error=true;
			}
		}else if(line.contains("if")){
			i=2;
			while(i<n){
				if(line.substring(i-2,i).equals("if")) break;
				i++;
			}
		}

		int csbi=0, osbi=line.indexOf('(');
		if(line.charAt(i)==' ') i++;
		if(line.charAt(i)=='('){
			bracketPos.add(i);
			i++;
			osb=1;			
		}else{
			System.out.println("Error at line number "  + (l+1) + " : missing '('.");
			print_error(line, i, tab_num);
			error=true;
			osb=-1;				
		}

		//travelling till length-1 coz { or ; is the last character
		int end=n-1;
		if(!line.endsWith(";") || !line.endsWith("{")) end=n;
		if(osb==1){
			for(;i<end;i++){
				if(line.charAt(i)=='('){
					osb++;
					bracketPos.add(i);				
				} 
				else if(line.charAt(i)==')') {
					osb--;
					if(bracketPos.size() > 0) bracketPos.remove(bracketPos.size()-1);
					if(osb==0) {
						csbi=i;
						break;
					}
				}
			}
			if(osb==0) i++;
		}
		// closing bracket ) found but there are extra characters btw ) and {
			String str="";
		if(line.endsWith("{") && osb==0){
			for(; i<line.length()-1;i++){
				str+= line.charAt(i);
			}
			if(!str.equals(" ") && !str.equals("")){
				System.out.println("Error at line number "+ (l+1) + " :  Extra \""+ str +"\" between ELSE and '{'.");
				print_error(line, i-1, tab_num);			
				error=true;
			}
		}
		// un closed brackets
		if(osb>0){
			while(osb!=0){
				osb--;
				System.out.println("Error at line number "+ (l+1)  + " :  No closing bracket ')' for corresponding '('." );
				print_error(line, bracketPos.get(bracketPos.size()-1), tab_num);
				error=true;
				bracketPos.remove(bracketPos.size()-1);
			}
		}
		// check boolean expression
		if(osb==0 ){
			if(csbi!=0&&!checkboolexpr(line.substring(osbi,csbi+1))){
				System.out.println("Error at line number "+ (l+1)  + " :  Invalid boolean expression." );
				System.out.println(line);
				error=true;
			}
		}
		// if not ending with either { or ; 
		if(!line.endsWith("{") && !line.endsWith(";")){
			System.out.println("Error at line number "  + (l+1) + " :  missing ';' at end of statement.");
			print_error(line, line.length()-1, tab_num);
			error=true;
		}
	}
	public static void else_check(){
		int b=4, n=line.length();
		if(!el_possible){
			System.out.println("Error at line number " + (l+1) + " :  This ELSE statement doesn't correspond to any IF statement." );
			error=true;
		}
		while(b<n){
			if(line.substring(b-4,b).equals("else")) break;
			b++;
		}
		String str="";
		for(; b<line.length()-1;b++){
			str+= line.charAt(b);
			
		}
		if(!str.equals(" ") && !str.equals("")){
				System.out.println("Error at line number "+ (l+1) + " :  Extra \""+ str +"\" between ELSE and '{'.");
				print_error(line, b-1, tab_num);			
				error=true;
				}
	}	
	public static void while_check(){
		int i = 0, n=line.length();
		osb=0;
		bracketPos.clear();

			i=5;
			int csbi=0, osbi=line.indexOf('(');
			
			while(i<n){
				if(line.substring(i-5,i).equals("while")) break;
				i++;
			}

			if(line.charAt(i)==' ') i++;
			if(line.charAt(i)=='('){
				bracketPos.add(i);
				i++;
				osb=1;			
			}else{
				System.out.println("Error at line number "  + (l+1) + " : Missing '(' in WHILE loop syntax.");
				print_error(line, i-1, tab_num);
				error=true;
				osb=-1;				
			}
	
			//travelling till length-1 coz { or ; is the last character
			int end=n-1;
			if(!line.endsWith(";") || !line.endsWith("{")) end=n;
			if(osb==1){
				for(;i<end;i++){
					if(line.charAt(i)=='('){
						osb++;
						bracketPos.add(i);				
					} 
					else if(line.charAt(i)==')') {
						osb--;
						if(bracketPos.size() > 0) bracketPos.remove(bracketPos.size()-1);
						if(osb==0) {
							csbi=i;
							break;
						}
					}
				}
				if(osb==0) i++;
			}
			if(while_possible){
				// closing bracket ) found but there are extra characters btw ) and {
					
				if(line.endsWith("{") && osb==0){
					for(;i<line.length()-1;i++){
						if(line.charAt(i)!=' '){
							System.out.println("Error at line number "+ (l+1) + " :  Extra charecters between ')' and '{'.");
							print_error(line, i, tab_num);
							error=true;
						}
					}
				}
				// un closed brackets
				if(osb>0){
					while(osb!=0){
						osb--;
						System.out.println("Error at line number "+ (l+1)  + " :  No closing bracket ')' for corresponding '('." );
						print_error(line, bracketPos.get(bracketPos.size()-1), tab_num);
						error=true;
						bracketPos.remove(bracketPos.size()-1);
						return;
					}
				}
				// check boolean expression
				if(osb==0){
					if(!checkboolexpr(line.substring(osbi,csbi+1))){
						System.out.println("Error at line number "+ (l+1)  + " :  Invalid boolean expression." );
						System.out.println(line);
						error=true;
					}
				}			
				// if not ending with either { or ; 
				if(!line.endsWith("{") && !line.endsWith(";")){
					System.out.println("Error at line number "  + (l+1) + " :  Missing ';' at end of statement.");
					print_error(line, line.length()-1, tab_num);
					error=true;
				}	
			}else{
				if(i<n && line.charAt(i)==' ') i++;
				if(osb==0){
					if(!checkboolexpr(line.substring(osbi,csbi+1))){
						System.out.println("Error at line number "+ (l+1)  + " :  Invalid boolean expression." );
						System.out.println(line);
						error=true;
					}
				}				
				if(i<n && line.charAt(i)!=';'){
					System.out.println("Error at line number "  + (l+1) + " :  Missing ';' after of while statement in do-while loop.");
					error=true;
				}
			}
	}
	public static void do_while_check_func(){
		int i=2, n=line.length();
		while(true){
			if(line.substring(i-2,i).equals("do")) break;
			i++;
		}		
		if(i<n && line.charAt(i)==' ') i++;
		if(i<n && line.charAt(i)!='{'){
			System.out.println("Error at line number " + (l+1) + " :  Missing '{' after do statement." );
			print_error(line, i, tab_num);
			error=true;
		}
	}
	public static boolean checkupdexpr(String stmt){
		  if(stmt.equals(";")){
			return true;
		  }
		  int[] ind = new int[6];
		  ind[0] = stmt.indexOf("++");
		  ind[1] = stmt.indexOf("--");
		  ind[2] = stmt.indexOf("+=");
		  ind[3] = stmt.indexOf("-=");
		  ind[4] = stmt.indexOf("*=");
		  ind[5] = stmt.indexOf("/=");
		  if(ind[0] == ind[2]){
			ind[2] = -1;
		  }
		  if(ind[1] == ind[3]){
			ind[3] = -1;
		  }
		  int ther = 0;
	  
		  for(int i =0; i<6; i++){
			if(ther == 0 && (ind[i] != -1)){
			  ther = 1;
			}else if(ther == 1 && (ind[i] != -1)){
			  return false;
			}
		  }
	  
		  return ther==1;		
	}
    public static boolean checkboolexpr(String stmt){
		if(stmt.equals(";")){
		  return true;
		}
		int[] ind = new int[6];
		ind[0] = stmt.indexOf(">");
		ind[1] = stmt.indexOf("<");
		ind[2] = stmt.indexOf(">=");
		ind[3] = stmt.indexOf("<=");
		ind[4] = stmt.indexOf("==");
		ind[5] = stmt.indexOf("!=");
		if(ind[0] == ind[2]){
		  ind[2] = -1;
		}
		if(ind[1] == ind[3]){
		  ind[3] = -1;
		}
		int ther = 0;
	
		for(int i =0; i<6; i++){
		  if(ther == 0 && (ind[i] != -1)){
			ther = 1;
		  }else if(ther == 1 && (ind[i] != -1)){
			return false;
		  }
		}
	
		return ther==1;
	  }
	public static void for_check(){
		int i=3, start_line=l,n=line.length();
		while(true){
			if(line.substring(i-3,i).equals("for")) break;
			i++;
		}

		//checking for '(' of for loop 
		if(line.charAt(i)==' ') i++;
		if(line.charAt(i)=='('){
			bracketPos.add(i);
			i++;		
		}else{
			System.out.println("Error at line number " + (l+1) + " :  Missing '('." );
			print_error(line, i, tab_num);
			error=true;
			return;				
		}	
		int sc_cnt=0;
		int osb = 1;
		while(i<n && sc_cnt!=3 && osb!=0){
			if(line.charAt(i)==';') sc_cnt++;
			if(line.charAt(i)=='(') osb++;
			if(line.charAt(i)==')') osb--;
			
			i++;
		}
		if(sc_cnt!=2){
			System.out.println("Error at line number " + (l+1) + " : FOR loop should have 2 semi-colons." );
			print_error(line, i, tab_num);
			//System.out.println(i);
			error=true;
			return;	
		}
		int osb_ind= line.indexOf('(');
		osb=1;
		String nline="";sc_cnt=0;
		int j=osb_ind+1;
		if(line.charAt(j)==' ') j++;
		if(line.substring(j,j+3).equals("var")) j+=3;
		for(;j<n;j++){
			if(line.charAt(j)=='(') osb++;
			if(line.charAt(j)==')') osb--;
			if(osb==0) break;
			nline+= line.charAt(j);
			if(line.charAt(j)==';'){
				sc_cnt++;
				if(sc_cnt==1){
					is_stmt(nline,0);
				}else if(sc_cnt==2){
					if(!checkboolexpr(nline)){
						System.out.println("Error at line number " + (l+1) + " : Invalid boolean expression." );
						print_error(line, j, tab_num);
						error=true;					
					}
				}
				nline="";
			}	
		}
		
		if(!checkupdexpr(nline)){
			System.out.println("Error at line number " + (l+1) + " : Invalid update expression." );
			print_error(line, j-1, tab_num);
			error=true;					
		}	
		if(j==n){
			System.out.println("Error at line number " + (l+1) + " : Missing ')' of FOR loop." );
			print_error(line, j, tab_num);
			error=true;			
		}
	}
	public static void print_error(String line,int i, int tab_num){
		for(int h=0; h<tab_num;h++){
			System.out.print("\t");
		}
		System.out.println(line);
		for(int h=0; h<tab_num;h++){
			System.out.print("\t");
		}
		for(int h=0; h<i; h++) System.out.print(" ");
		System.out.println("^");
	}
	public static void store_comments_len(String fileName) throws IOException{
        FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		String Line;
		int index=0;
		
		while((Line=br.readLine())!=null){
			if(Line.contains("//")){
				index=0;
				while(index<Line.length()){
					if(Line.charAt(index)=='/'&&Line.charAt(index+1)=='/') break;
					index++;
				}
				comments_len.add(Line.length()-index);
			}
		}
	}
	public static String js_code_to_string(String fileName) throws IOException{
		String whole_code="", Line;
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		while((Line=br.readLine())!=null){
			whole_code += Line + " ";
		}
		return whole_code;
	}
	public static void format_comments(){
		while(whole_code.charAt(code_itr)=='/'&&whole_code.charAt(code_itr+1)=='/'){
			if(!reformatted_code_new_line.equals("") && !reformatted_code_new_line.equals(" ")){
				if(reformatted_code_new_line.charAt(0)==' ' || reformatted_code_new_line.charAt(0)=='\t') reformatted_code.add(reformatted_code_new_line.substring(1));
				else reformatted_code.add(reformatted_code_new_line);
			}
			reformatted_code_new_line="";
			reformatted_code.add(whole_code.substring(code_itr, code_itr+comments_len.get(comment_index)));
			code_itr+= comments_len.get(comment_index);
			if(code_itr>=whole_code_len) return;
			comment_index++;
		}
	}
	public static void remove_unwanted_spaces(){
		while(code_itr<whole_code_len && (whole_code.charAt(code_itr)=='\t'||whole_code.charAt(code_itr)==' ')){
			code_itr++;
		}
		if(code_itr!=whole_code_len) reformatted_code_new_line += " ";
		if(code_itr>=whole_code_len) return;
	}
	public static void reformat_code(){
		code_itr=0;
		while(code_itr<whole_code_len){
			//taking comments in newline
			format_comments();
			if(code_itr>=whole_code_len) return;
			//converting multiple spaces or tabs into a single space
				if(whole_code.charAt(code_itr)=='\t'||whole_code.charAt(code_itr)==' '){
					remove_unwanted_spaces();
				}else{
					reformatted_code_new_line += whole_code.charAt(code_itr);
					//splitting the code such that every statement ends with ';' or '{' or '}'
					//break the line when ';' or '{' or '}' is encountered
					if(whole_code.charAt(code_itr)==';' || whole_code.charAt(code_itr)=='{' || whole_code.charAt(code_itr)=='}'){
						if(reformatted_code_new_line.charAt(0)==' ' || reformatted_code_new_line.charAt(0)=='\t') reformatted_code.add(reformatted_code_new_line.substring(1));
						else reformatted_code.add(reformatted_code_new_line);
						reformatted_code_new_line=""; 
					}
					code_itr++;			
					if(code_itr>=whole_code_len) return;
				}


		}
		if(!reformatted_code_new_line.equals("")) reformatted_code.add(reformatted_code_new_line); //taking the last line that might not end with ; or { or }
		
	}
	//updates the code.js file with reformatted code
	public static void reformat_input_file(String fileName)throws IOException{
		try{
			
			FileWriter fw= new FileWriter(fileName);
			for(String name: reformatted_code)
			{
				if(name.endsWith("}")) tab_num--;
				for(int f=0; f<tab_num; f++){
					fw.write("\t");
				}
				fw.write(name+"\n");
				if(name.endsWith("{")) tab_num++;
			}
			fw.close();
		}catch(Exception e){System.out.println(e);}  
	}
	public static boolean curl_bracket_balancing(){
		tab_num=0;
		for(int j=0; j<len;j++){
			
			line= reformatted_code.get(j);
			if(line.contains("//")) continue; //for comments
			if(line.endsWith("}")) tab_num--;

			
				if(line.endsWith("{")) {
					ocb++;
					bracketPos.add(j);
				}
				else if(line.endsWith("}")) {
					if(ocb==0){
						System.out.println("Error at line number " +(j+1) + " : Extra closing bracket.");
						print_error(line, line.length()-1, tab_num);
						error=true;
						return false;					
					}
					ocb--;
					if(bracketPos.size() > 0) bracketPos.remove(bracketPos.size()-1);			
				}
				
			
			if(line.endsWith("{")) tab_num++;
		}
		if(ocb>0){
			while(ocb!=0){
				System.out.println("Error: Bracket '{' is not closed at line number: "+(bracketPos.peek()+1));
				ocb--;
				bracketPos.pop();
				error=true;
			}
			return false;
		}
		return true;
	}
	public static void bracket_balance(String fileName) throws IOException{
		//doing bracket balancing in the code.
		FileReader fr = new FileReader(fileName);
		Stack <Character> bracket = new Stack<Character>();
		Stack <Integer> bracketLine = new Stack<Integer>() ;
		Stack <Integer> errorIndx = new Stack<Integer>() ;
		Stack <String> correspondingLine = new Stack<String>() ;
		BufferedReader br = new BufferedReader(fr);
		String line;
		int lineNumber = 0;
		int tempLine = 0;
		boolean dq = false;
		while((line = br.readLine()) != null){
			lineNumber++;
			int n = line.length();
			for(int i = 0; i<n ;i++){
				if(line.charAt(i)=='"') dq = !dq ;
				if(line.charAt(i)=='['||line.charAt(i)=='{'||line.charAt(i)=='('){
				if(dq||(i!=0&&i!=(n-1)&&line.charAt(i-1)=='\''&&line.charAt(i+1)=='\'')) continue;
					
					bracket.push(line.charAt(i));
					correspondingLine.push(line);
					errorIndx.push(i);
					//System.out.println(bracket.peek()+" "+lineNumber);
					bracketLine.push(lineNumber);
					
				}
				else if(line.charAt(i)==']'||line.charAt(i)=='}'||line.charAt(i)==')'){
					if(dq||(i!=0&&i!=(n-1)&&line.charAt(i-1)=='\''&&line.charAt(i+1)=='\'')) continue;
					
					//System.out.println(line.charAt(i)+" "+lineNumber);
					if(bracket.isEmpty()){
						System.out.println("Extra closing bracket at line number "+lineNumber+".");
						int p=0;
						while(line.charAt(p)=='\t'){
							p++;
						}
						System.out.println(line.substring(p));
						for(int j = 1; j<i-p ; j++)System.out.print(" ");
						System.out.println("^");
						error=true;
						return;
						
					}
					else{
						
						if(bracket.peek() == '(' && line.charAt(i)!=')'){
							System.out.println("Missing ')' at line number "+lineNumber+".");
							
							int p=0;
							while(line.charAt(p)=='\t'){
								p++;
							}
							System.out.println(line.substring(p));
							for(int j = 1; j<i-p ; j++)System.out.print(" ");
							System.out.println("^");
							
							System.out.println("Coressponding opening Bracket '"+bracket.peek()+"' is not closed at line "+bracketLine.peek()+".");
							p=0;
							while(correspondingLine.peek().charAt(p)=='\t'){
								p++;
							}
							System.out.println(correspondingLine.peek().substring(p));
							for(int j = 0; j<errorIndx.peek()-p ; j++)System.out.print(" ");
							System.out.println("^");
							error=true;
							return;
						}
						else if(bracket.peek() == '(' && line.charAt(i)==')'){
							bracket.pop();
							bracketLine.pop();
							correspondingLine.pop();
							errorIndx.pop();
							
						}else
						
						if(bracket.peek() == '{' && line.charAt(i)!='}'){
							System.out.println("Missing '}' at line number "+lineNumber+".");
							int p=0;
							while(line.charAt(p)=='\t'){
								p++;
							}
							System.out.println(line.substring(p));
							for(int j = 1; j<i-p ; j++) System.out.print(" ");
							System.out.println("^");
							System.out.println("Coressponding opening Bracket '"+bracket.peek()+"' is not closed at line "+bracketLine.peek()+".");
							p=0;
							while(correspondingLine.peek().charAt(p)=='\t'){
								p++;
							}
							System.out.println(correspondingLine.peek().substring(p));
							for(int j = 0; j<errorIndx.peek()-p ; j++)System.out.print(" ");
							System.out.println("^");
							error=true;
							return;
						}
						else if(bracket.peek() == '{' && line.charAt(i)=='}'){
							bracket.pop();
							bracketLine.pop();
							correspondingLine.pop();
							errorIndx.pop();
							
						}else
						
						if(bracket.peek() == '[' && line.charAt(i)!=']'){
							System.out.println("Missing ']' at line number "+lineNumber+".");
							int p=0;
							while(line.charAt(p)=='\t'){
								p++;
							}
							System.out.println(line.substring(p));

							for(int j = 1; j<i-p ; j++)System.out.print(" ");
							System.out.println("^");
							System.out.println("Coressponding opening Bracket '"+bracket.peek()+"' is not closed at line "+bracketLine.peek()+".");
							p=0;
							while(correspondingLine.peek().charAt(p)=='\t'){
								p++;
							}
							System.out.println(correspondingLine.peek().substring(p));
							for(int j = 0; j<errorIndx.peek()-p ; j++)System.out.print(" ");
							System.out.println("^");
							error=true;
							return;
						}
						else if(bracket.peek() == '[' && line.charAt(i)==']'){
							bracket.pop();
							bracketLine.pop();
							correspondingLine.pop();
							errorIndx.pop();
							
						}
					}
					
				}
			}
		}
		if(!bracket.empty()){
			
			while(!bracket.empty()){
				System.out.println("Bracket '"+bracket.peek()+"' is not close at line :"+bracketLine.peek()+".");
				int p=0;
				while(correspondingLine.peek().charAt(p)=='\t'){
					p++;
				}
				System.out.println(correspondingLine.peek().substring(p));
				for(int j = 0; j<errorIndx.peek()-p ; j++)System.out.print(" ");
				System.out.println("^");
				
				bracket.pop(); bracketLine.pop();correspondingLine.pop();errorIndx.pop();
			}
			error=true;
			return;
		}
//code is bracket balanced	
	}
	
    public static void main(String[] fileName) throws IOException {

		//System.out.println(fileName[0]);
		
		
	try{
		
		bracket_balance(fileName[0]);
		if(error) return;
		
		store_comments_len(fileName[0]);

		//Re formatting the code.js file so that it is easy to do parsing...
		//reading the code through buffered reader and storing it in whole_code as string
		whole_code =  js_code_to_string(fileName[0]);
		whole_code_len=whole_code.length();

		comment_index=0; // pointing to index 0 of comments_len
		//reformat the code and store it in reformatted_code
		reformat_code();

		for(int x=0; x<reformatted_code.size();x++){
			if(reformatted_code.get(x).contains("for")){
				int cnt=0;
				boolean add=false;
				String nl="", nl1, cur_stmt;
				int osbi=0;
				for(int y=0; y<3; y++){
					cur_stmt= reformatted_code.get(x);
					for(int k=0; k<cur_stmt.length(); k++){
						if(cur_stmt.charAt(k)=='(') osbi++;
						else if(cur_stmt.charAt(k)==')') osbi--;
					}
					if(reformatted_code.get(x).contains(")") && osbi==0){
						if(!reformatted_code.get(x).endsWith("{")){
							nl += reformatted_code.get(x).substring(0, reformatted_code.get(x).indexOf(")")+1);
							nl1= reformatted_code.get(x).substring(reformatted_code.get(x).indexOf(")")+1);
							
							nl+= reformatted_code.get(x);
							reformatted_code.remove(x);
							reformatted_code.add(x,nl);
							reformatted_code.add(x+1,nl1);	
							
						}else{
							nl+= reformatted_code.get(x);
							reformatted_code.remove(x);	
							reformatted_code.add(x, nl);
						
							
						}
						add=true;
						break;
					}else{
						nl+= reformatted_code.get(x);
						reformatted_code.remove(x);						
					}
				}
				if(!add){
					reformatted_code.add(x, nl);
					add=!add;
				}
				nl="";
			}
		}
		// changing the code.js file to reformatted code 
		reformat_input_file(fileName[0]);
	}
	catch(IOException e){
		System.out.println("File with Specified name NOT found.");
		return;
	}
		len = reformatted_code.size();


		tab_num=0;
		el_possible= false;
		bracketPos.clear();
		if_check.clear();
		for(; l<len; l++){
			//System.out.println(el_possible+" "+(l+1));
			line= reformatted_code.get(l);
			int n= line.length();
			if(line.contains("//")) continue; //for comments
			if(line.endsWith("}")) tab_num--;

			if(line.endsWith("{")){
				if(line.contains("if") || line.contains("else if")){
					if_check.push(1);
				}else{
					if_check.push(0);
				}
				
				if(line.contains("do")){
					do_while_check.push(l+1);
				}else{
					do_while_check.push(0);
				}
			}

			// checking errors in var declarations
			if(line.contains("var")){
				var_check();
			}
			// Check for errors in if/else statements	
			if (line.contains("if") || line.contains("else if")) {
				if_else_if_check();
			}else if (line.contains("else")) {
				else_check();
			}
			// checking errors in while loops
			if(line.contains("while")){
				while_check();
			}
			// checking errors in for loops
			if(line.contains("for")){
				for_check();
			}
			// checking errors in do-while loops
			if(line.contains("do")){
				do_while_check_func();
			}
			
			if(!while_possible) while_possible=true;
			if(el_possible) el_possible=false;
			if( (line.contains("if")&&line.endsWith(";")) || (line.contains("else if")&&line.endsWith(";"))){
				//System.out.println("yes"+(l+1));
				
				el_possible=true;
			}
			if(line.endsWith("}")){
				if(!if_check.empty() && if_check.peek()==1){
					//System.out.println("yes1"+(l+1));
					el_possible= true;
				}
				if(!do_while_check.empty() && do_while_check.peek()>0){
					String cur_stmt= reformatted_code.get(l+1);
					if( l+1>=len || !reformatted_code.get(l+1).contains("while")){
						System.out.println("Error at line number " + (do_while_check.peek()) + " : This DO statement doesn't have any corresponding WHILE statement." );
						error=true;
						return;
					}
/*					boolean cond= (reformatted_code.get(l+1).contains("while(")||reformatted_code.get(l+1).contains("while ("));
					

					if(l+1<len && !cond){
						System.out.println("Error at line number " + (l+2) + " : Incorrect while syntax in do-while loop." );
						error=true;	
						return;	
					}
					if(l+1<len && cond){
						int osbi, csbi;
						osbi= reformatted_code.get(l+1).indexOf('(');
						csbi= reformatted_code.get(l+1).indexOf(')');
						if(!checkboolexpr(reformatted_code.get(l+1).substring(osbi+1, csbi))){
							System.out.println("Error at line number " + (l+2) + " : Incorrect boolean expression inside while in do-while loop." );
							error=true;						
						}
					}
*/					
					while_possible=false;
				}
				do_while_check.pop();
				if_check.pop();
			}
			if(line.endsWith("{")) tab_num++;
		}	
			if(!error){
				System.out.println("\n***************************  Program Compiled Successfully!!!  ***************************");
				System.out.println("This program has correct syntax structures for declaratives, if-else statements and loops.");
			}
	}
}