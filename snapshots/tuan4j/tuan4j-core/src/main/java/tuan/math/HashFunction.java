package tuan.math;

public class HashFunction {
	
    /**
     * Create hashed value from the given 
     * parameter and seed.
     *  
     * NOTE: applied 64 bit hash function by Thomas Wang.
     * 
     * @param value input
     * @param hashSeed seed value for hash function
     * @return hashed value
     */
    public long wanghash(final long value, final long hashSeed) {  
        Long key = (value+hashSeed);
        key = (~key) + (key << 21);
        key = key ^ (key >>> 24);
        key = (key + (key << 3)) + (key << 8); 
        key = key ^ (key >>> 14);
        key = (key + (key << 2)) + (key << 4); 
        key = key ^ (key >>> 28);
        key = key + (key << 31);
        return key;
    }  
    
    
}
