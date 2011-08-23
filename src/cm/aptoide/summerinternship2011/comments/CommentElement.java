package cm.aptoide.summerinternship2011.comments;

/**
 * 
 * @author rafael
 *
 */
public enum CommentElement {
	
	RESPONSE, STATUS, LISTING, ENTRY , ID, USERNAME, ANSWERTO, SUBJECT, TEXT, TIMESTAMP, ERRORS;

	public static CommentElement valueOfToUpper(String name) {
		return CommentElement.valueOf(name.toUpperCase());
	}

}