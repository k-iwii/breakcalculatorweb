# Kallie's Debate Break Calculator
My name is Kallie! I'm a high school debater in Canada. This is my debate break calculator.

## Why This Calculator?
*Kallie's Debate Break Calculator isn't the only calculator out there. Why should you use mine?*

My calculator is the *ONLY one on the Internet* that can...
* Calculate the **junior/novice** division break, in addition to the open division break
* Automatically find the **number of teams** at the tournament: no more manually counting through dozens, or even hundreds, of teams!
* Handle tournaments in the **British Parliamentary** AND **World Schools** debate formats
* Update its predictions to become **more accurate** as the tournament progresses
* Produce **more accurate** results than all other calculators, even without accounting for the features above!

## The Idea: What's a Debate Break Calculator Anyway?
Like many high schoolers around the world, I participate in competitive debate tournaments.

These tournaments are generally divided into two sections: in-rounds and out-rounds.
During the in-rounds, teams compete in power-paired debates and collect points based on their performance. For example, in the British Parliamentary (BP) debate format, the team who wins 1st place gets 3 points; the team who wins 2nd place gets 2 points; the team who takes 3rd gets 1 point; the team who takes 4th gets no points. Debates are power-paired. This means that, before each in-round begins, teams are effectively sorted by the number of total points that they have accumulated across all the previous rounds, and then assigned into rooms to debate together. So, for a BP debate tournament, the first 4 teams might be grouped into the top room, and then the next 4 into the second room, ... etc.
At the end of the in-rounds, the teams with the most points will qualify for out-rounds (typically, quarterfinals, semifinals, or finals). Qualifying for out-rounds is called 'breaking'. 

Often, while competing at a tournament, debaters are curious as to how many points they will need to break at the tournament: ie. what is the minimum number of points that a debater can have, while still breaking? (For the rest of this document, I will refer to this number as 'the threshold'.)
This is the debate break calculation problem that I wanted to solve with my calculator.

## A Closer Look: What Makes the Problem so Challenging?
This problem is a bit more complicated than it may appear. Given two equivalent tournaments that have the same number of teams,  in-rounds, and teams that break, the threshold can vary depending on how the debaters collectively perform at the tournament. 

The hypothetical best case (in which the threshold is as low as possible) will occur if the top [(number of breaking teams) - 1] teams accumulate as many points as possible, and the remaining points are dispersed roughly evenly among the other teams. Consider a 16-team, 5-in-round BP tournament where 4 teams break: there are 120 total points available across the in-rounds. The hypothetical best case would occur if the top 3 teams each won all 5 of their debates, thus ending the in-rounds with 15 points each. In this ideal scenario, the remaining points would be dispersed evenly among the other teams. Thus, the 4th breaking team, like all the other teams in the tournament, would have 5.76 points. This is the hypothetical lowest threshold.
* Theoretically, this should occur if there are [(number of breaking teams) - 1] teams that are much stronger than the other teams, who are all roughly equal in skill to each other.
* But the above scenario actually wouldn't lead to this hypothetical best case, which is why the debate break calculation problem is so confusing. The top X teams of the tournament would inevitably end up competing against each other, and they would inevitably leave that room with very different point numbers.
* This is why it's impossible for 3 teams to end the in-rounds with 15 points, and why this solution is only hypothetical.

The hypothetical worst case will occur if the top [(number of breaking teams) + 1] teams accumulate as many points as possible such that the point numbers of each team is as similar as possible, and the remaining teams have as few points as possible. Consider the same 16-team, 5-in-round BP tournament where 4 teams break. The hypothetical worst case would occur if all 120 available points were distributed evenly among the top 5 teams. In this case, even 24 points wouldn't be enough to guarantee that you break.
* Again, this scenario is impossible because it's not possible for a group of very skilled teams to keep winning forever at a debate tournament. Eventually, they will be matched up against each other, and someone will have to lose.

## My Solution
For the reasons illustrated above, I found it very difficult to find a rigourous solution to the debate break calculation problem. After many brainstorming sessions and several consultations with software engineers and mathematicians, I decided that an easier solution would be to simulate a tournament 10,000 times and to output the greatest and least values for the threshold that my simulation encountered.

My code extracts a tournament's data, given its Calicotab URL (Calicotab is the standard tabulation software used to host debate tournaments worldwide), simulates the tournament 10,000 times, and outputs the best and worst cases that it encountered.