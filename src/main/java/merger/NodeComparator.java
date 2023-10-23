package merger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

public class NodeComparator implements Comparator<Node> {
     
	private Comparator<Object> stringComparator = Comparator.comparing(Object::toString);
	
	List<Class<?>> classOrder = Arrays.asList( //
     PackageDeclaration.class //
     , ImportDeclaration.class //
     , ClassOrInterfaceDeclaration.class //
     , BodyDeclaration.class //
     , FieldDeclaration.class //
     , ExplicitConstructorInvocationStmt.class // 
     , BlockStmt.class //
     , ExpressionStmt.class //
     , LineComment.class //
     , ReturnStmt.class //
     );

	@Override
	public int compare(Node o1, Node o2) {
		Integer compare = null; 
		for (int i = 0; i < classOrder.size() && compare == null; i++) {
			Class<?> claz = classOrder.get(i);
			boolean o2Assignable = claz.isAssignableFrom(o2.getClass());
			boolean o1Assignable = claz.isAssignableFrom(o1.getClass());
			if (o1Assignable && o2Assignable) {
				compare = 0; 
			} else if(o1Assignable) {
				compare = -1; 
			} else if (o2Assignable) {
				compare = 1; 
			}
		}
		if (compare ==null) {
			compare = stringComparator.compare(o1, o2);
		}
		return compare;
	}

}
