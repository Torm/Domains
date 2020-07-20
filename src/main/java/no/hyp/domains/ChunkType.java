package no.hyp.domains;

/**
 * Represents how a chunk is claimed.
 */
public enum ChunkType {

    /**
     * There are no claims in this chunk.
     */
    UNCLAIMED,

    /**
     * The entire chunk is claimed as a whole.
     */
    EXCLUSIVE,

    /**
     * The chunk is partitioned into columns which can be claimed separately.
     * A column is a 1 × 256 × 1 region of blocks.
     */
    PARTITIONED,

}
