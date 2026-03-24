/**
 * 
 */
/**
 * 
 */
module ccsh {
	requires com.sun.jna;
	requires com.sun.jna.platform;

	exports ccsh;

	opens ccsh to com.sun.jna;
}