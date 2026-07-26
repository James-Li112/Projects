These are the Java projects I have worked on. They were done in CS10, Object Oriented Programming in Java.

Viterbi.java: An NLP tagger that labels words with their grammatical role using a Hidden Markov Model.

Uses the Viterbi algorithm to find the most likely tag sequence for a sentence, training on annotated text, including the Brown corpus, and handling word-sense ambiguity through context. Demonstrates HMMs, dynamic programming, and log-probability math.


HuffmanTree.java: A lossless file compressor that shrinks files by giving frequent characters shorter bit codes.

Builds a frequency table from the input, uses a priority queue to construct a binary code tree by repeatedly merging the two lowest-frequency subtrees, and traverses the tree to map each character to a prefix-free code word. Compresses by writing out bit codes and decompresses by walking the tree bit-by-bit. Tested on the US Constitution and War & Peace.

Supplementary files: BufferedBitReader.java, BufferedBitWriter.java, CodeTreeElement.java, Huffman.java


GraphLibrary.java: A graph-based tool that finds the shortest connection between any actor and a chosen "center of the universe."

Builds a graph from movie data (9,235 actors, 32,337 edges) where actors are vertices and shared films are edges, then runs breadth first search to construct a shortest path tree back to the center. Supports interactive queries, such as finding an actor's "Bacon number," listing best/worst centers by average separation or degree, and identifying disconnected actors. 

Supplementary files: AdjacencyMapGraph.java
