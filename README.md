This repository contains example files I use in my JDK8 / Lambdas / Streams / 
Collectors talks. 

The data files mcdonalds.csv, movies-mpaa.txt, ospd.txt and 
words.shakespeare.txt can be freely downloaded from Robert Sedgwick page
here: http://introcs.cs.princeton.edu/java/data/. By the way, there are other
very interesting data sets on this page. 

You can find 3 examples here : 
- the Scrabble example, or "how good at Scrabble Shakespeare would have been?"
- the MacDonald example, or "Houston, we've got a problem"
- the Movie database example. 

All files are provided under the GPL license. All data sets files are under the
copyright of their authors, and provided for convenience only.

More on "how good at Scrabble Shakespeare would have been". I realized that the 
best words are in fact not playable in Scrabble : not enough letters to 
place buzzards and whizzing on a Scrabble board. But there are two blanks 
letters in the Scrabble game. So let's take into account that in fact, buzzards
and whizzing are doable with blank letters, and let's change the computation
of the score to take into account that the blank letters score 0. 

Nice little problem, and it turns out that it is still solvable with a 
map / filter / reduce approach, thus fully lambda based. Great !  
