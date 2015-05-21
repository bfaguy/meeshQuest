
import java.lang.Exception;


/**
 * used in BPTree class
 * @author chen1279
 *
 */
public class DuplicateRecordException extends Exception {

	private Object duplicateRecord;
	
	public DuplicateRecordException(Object record){
		this.duplicateRecord = record;
	}
	
	public Object getOldRecord(){
		return this.duplicateRecord;
	}
	
}
