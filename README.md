Combining Free Monads with Free Applicatives workshop  
=====================================================

Running

Command Line:
```
sbt "run monad"
sbt "run applicative"
```

IDE:
Launch `app.Main`

# Problem
Given two Twitter accounts, determine which one has the most followers
who have tweeted in the last week.  Then return a url in the form 
`http://<app-url>/details/<twitter-handle>`.  Note that this endpoint 
does not exist, but assume later we might develop one.

# Initial solution
A solution has been provided (loosely based on the interpreter pattern
at REA). However the solution has some limitations.

# Intellij IDE notes
Note that some of the valid code in this solution will be highlighted
by intellij as having structural issues.  Especially around the case 
statement in the interpreter. 
https://youtrack.jetbrains.com/issue/SCL-3170

Intellij also gets confused by the `|@|` operator when combining 
applicatives.  Any alternative is to use `Apply[M].map2(a, b)(handleBoth)` 

# Monix Notes
The problem makes use of Monix tasks as an abstraction over Scala Future.
Monix has been used as it cleanly separates the intention of asynchronous
execution from the actual execution.  A Scala Future does not provide
this level of flexibility. 

See https://monix.io/docs/2x/eval/task.html.

## 1. Single ADT
We have defined a single top level ADT which becomes bloated with all 
possible operations.  A single top level interpreters needs to 
concern itself with all operations.

## 2. Monadic composition
All side effects are monadic.  A monad defines flatMap (aka Bind) as:
```
trait Monad[A] {
   def flatMap[B](f: A => F[B]): F[B]   
```

So when doing 
```
for {
   lukeFollowers <- getFollowers("lukestephenson")
   composeFollowers <- getFollowers("composemelbourne")
} yield ()
```

Even though there is no logical dependency between the two lookups for 
followers, the monadic chaining introduces one.

Given there is no logical dependency between the two lookups, our 
logic should ideally reflect that.

# Exercises
Tackle either of the above.

For problem 1 refer to http://typelevel.org/cats/tut/freemonad.html 
under the heading *Composing Free monads ADTs.*

For problem 2, see http://typelevel.org/cats/tut/freeapplicative.html.  
Note that you will still need monadic composition as well (to fetch the
most recent tweet for each follower).

For this problem, there is a branch `free-applicative` to use as a 
starting point.

