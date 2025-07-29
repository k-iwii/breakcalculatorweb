# Kallie's Debate Break Calculator
Hi there! My name is Kallie, and I'm a high school debater in Canada. You can use my debate break calculator **here** -> https://kallie-break-calculator-deb8bdc1621f.herokuapp.com/

## What is Debate Break Calculation?
Debate tournaments are generally divided into two sections: in-rounds and out-rounds.
During the in-rounds, teams compete in **power-paired** debates and collect **points** based on their performance.
* Ex: In the British Parliamentary (BP) debate format, the 1st place team gets 3 points; the 2nd place team gets 2 points; 3rd place gets 1 point; 4th gets no points.
* Debates are power-paired. So, before each round begins, teams are sorted by the number of total points that they have accumulated across all the previous rounds, and then assigned into rooms to debate together. For a BP debate tournament, the first 4 teams might be grouped into the top room, and then the next 4 into the second room, ... etc.
At the end of the in-rounds, the teams with the most points will qualify for out-rounds (typically, quarterfinals, semifinals, or finals). Qualifying for out-rounds is called 'breaking'. 

Often, while competing at a tournament, debaters are curious as to **how many points** they will need to break at the tournament: ie. *what is the minimum number of points that a debater can have, while still breaking*? (For the rest of this document, I will refer to this number as 'the threshold'.)

This is the **debate break calculation** problem that I wanted to solve with my calculator.

This problem is a bit more complicated than it may appear. Given two equivalent tournaments that have the same number of teams,  in-rounds, and teams that break, the threshold can vary depending on how the debaters collectively perform at the tournament. As a result,  

## My Solution
I found it very difficult to find a rigourous solution to the debate break calculation problem. After many brainstorming sessions and several consultations with software engineers and mathematicians, I decided that an easier solution would be to simulate a tournament 10,000 times and to output the greatest and least values for the threshold that my simulation encountered.

My code extracts a tournament's data, given its Calicotab URL (Calicotab is the standard tabulation software used to host debate tournaments worldwide), simulates the tournament 10,000 times, and outputs the best and worst cases that it encountered.

The best and worst case for junior break are calculated as follows.
* Best Case: Assume that all open-breaking teams are junior teams, and that the remaining junior teams place as poorly in the tournament as possible, occupying all the lowest ranks. Imagine a list of the teams at this tournament, sorted by the number of points they have. There will be a 'chunk' of junior teams at the very top of the list, and a larger 'chunk' at the bottom, with open teams in the middle. If you go to the top of the lower chunk of junior teams, and count (number of junior-breaking teams) teams down, the number of points that that team has will be the junior threshold.
* Worst Case: Assume that all open-breaking teams are open teams, and that the remaining junior teams occupy the next ranks immediately after the first (number of open-breaking teams) teams. If you imagine the same list as before, the whole tournament would look like a chunk of open teams, then a chunk of junior teams, then a chunk of open teams. If you go to the top of the chunk of junior teams, and count (number of junior-breaking teams) teams down, the number of points that that team has will be the junior threshold.

If the tournament is currently ongoing, my program will begin to simulate the tournament from the middle onward. This means that my simulator becomes increasingly precise as the tournament progresses.

## Key features
Kallie's Debate Break Calculator can...
* Calculate the **junior/novice** division break, in addition to the open division break
* Automatically find the **number of teams** at the tournament (No more manually counting through dozens, or even hundreds, of teams. Yay!)
* Handle tournaments in the **British Parliamentary** AND **World Schools** debate formats
* Update its predictions to become **more accurate** as the tournament progresses
* Produce **more accurate** results than all other calculators, even without accounting for the features above!