# RX – A Term-Rewriting Language

RX (**R**ewrite e**X**pressions) is a small interpreter for a custom term-rewriting language. It is designed to explore how fundamental programming concepts can be represented and executed via rule-based rewriting of expressions.

The goal is not to build a production-ready language, but to study and experiment with how term rewriting interacts with typical features of programming languages such as control flow, recursion, and evaluation strategies.

> [!NOTE]  
> This project is part of the module "Programmiersprachen: Konzepte und Realisation" at the Technische Hochschule Mittelhessen

## Features

- User-defined rewrite rules with pattern matching
- Support for integer, float, boolean, character and string literals
- Arithmetic operations: `+`, `-`, `*`, `/`, `%`
- Boolean comparisons: `==`, `!=`, `<`, `<=`, `>`, `>=`
- String operations: `concat`, `length`, `charAt`
- Character operations: `toInt`
- REPL for testing the evaluation of expressions and adding of new rules
- Wildcards in rule definition to prevent substitution (eg. ``` def if(true, thenBranch, _) = thenBranch ```)
- A trace mode in the REPL to be able to trace the exact replacement steps individually
- Various steps to harden RX (eg. Errormessages, Prevent ambivalent rules from being added, etc.)
- Loading of individual modules - outsourcing of rule sets to independent modules, differentiation between internal and custom modules

### Currently WIP
- Add a namespace system to the module system to improve organization and prevent naming conflicts.
- Refactoring of REPL + Syntax-Highlighting

### Planned features
- Debug-Mode -> Detailed logging of the entire process from the interpreter
- Various modules that natively extend the functions of RX
  - More complex data structures such as lists
  - lambda calculus translators
  - binary operations
  - advanced mathematical operations
  - unit-test framework + Testmode in the interpreter
  - Converter for units of measurement
  - ...
- Providing more “syntactic sugar”
  - Is converted internally by the parser into a call of rewrite rules
  - Possible:
    - ```[]``` -> Easier creation of lists
    - ```&&```, ```||```, ```!``` -> Linking of boolean comparisons
    - ```"Hello, ${name}!"``` -> String-Interpolation
- Ideas for possible further features are constantly emerging :)

## Examples
> [!WARNING]  
> The following highlighting uses haskell as a preset -> This does not correspond to the correct highlighting in all cases. Unfortunately not possible otherwise, as inlinecss is sanitized by Github.
### 1. Conditional Expressions
```haskell
def if(true, thenBranch, _) = thenBranch
def if(false, _, elseBranch) = elseBranch

if(5 < 10, 1, 0)
```
**Result:** 1

### 2. Maximum and Minimum
```haskell
def if(true, thenBranch, _) = thenBranch
def if(false, _, elseBranch) = elseBranch

def max(a, b) = if(a > b, a, b)
def min(a, b) = if(a < b, a, b)

max(5, 12)
min(5, 12)
```
**Result of max(5, 12):** 12\
**Result of min(5, 12):** 5

### 3. Recursion
```haskell
def factorial(0) = 1
def factorial(n) = n * factorial(n - 1)

factorial(5)
```
**Result:** 120

### 4. Simple Arithmetic Function
```haskell
def square(x) = x * x

square(4)
```
**Result:** 16

### 5. Working with lists
<details>
  <summary>Rules from the prelude</summary>
    ```haskell
  
    def Nil() = Nil()
    def Cons(h, t) = Cons(h, t)
    
    def length(Nil()) = 0
    def length(Cons(h, t)) = 1 + length(t)
    
    def sum(Nil()) = 0
    def sum(Cons(h, t)) = h + sum(t)
    
    def reverse(xs) = rev(xs, Nil())
    def rev(Nil(), acc) = acc
    def rev(Cons(h, t), acc) = rev(t, Cons(h, acc))
    
    def head(Cons(h, t)) = h
    def tail(Cons(h, t)) = t
    def last(Cons(h, Nil())) = h
    def last(Cons(h, t)) = last(t)
    ```
</details>

```haskell
def testList() = Cons(1000, Cons(300, Cons(30, Cons(7, Nil()))))

sum(testList())
length(testList())
head(testList())
```
**Result of sum:** 1337\
**Result of length:** 4\
**Result of head:** 1000

### 6. Example-Output from the Tracemode
```haskell
def if(true, thenBranch, _) = thenBranch
def if(false, _, elseBranch) = elseBranch

def max(a, b) = if(a > b, a, b)

max(5, 2+1)
```
**Output**
```
> max(5,2+1)

[1] Expression: add[2, 1]
     Rule: [native rule] add[2, 1] -> 3
     Result: 3
[2] Expression: max[5, 3]
     Rule: max[a, b] -> if[GT(a, b), a, b]
     Result: if[GT(5, 3), 5, 3]
[3] Expression: gt[5, 3]
     Rule: [native rule] gt[5, 3] -> true
     Result: true
[4] Expression: if[true, 5, 3]
     Rule: if[true, thenBranch, _] -> thenBranch
     Result: 5

Initial Expression: max[5, ADD(2, 1)]
Result: 5
```


## Prelude
A set of basic definitions is loaded before each program:
```haskell
// Prelude - A collection of basic functions and data structures
// This file contains basic functions and data structures for functional programming.
// The code is divided into logical sections and commented to improve readability and maintainability.

// Control Structures
// These functions provide basic control structures for functional programming.
def if(true, thenBranch, _) = thenBranch
def if(false, _, elseBranch) = elseBranch

// Mathematical Operations
// These functions provide basic and advanced mathematical operations.
def max(a, b) = if(a > b, a, b)
def min(a, b) = if(a < b, a, b)
def square(x) = x * x
def abs(x) = if(x < 0, 0 - x, x)
def clamp(x, minVal, maxVal) = max(min(x, maxVal), minVal)
def double(x) = x * 2
def inc(x) = x + 1

// Number Predicates
// These functions check properties of numbers.
def isZero(x) = x == 0
def isPositive(x) = x > 0
def isNegative(x) = x < 0

// Recursive Mathematical Functions
// These functions use recursion to calculate mathematical operations.
def fact(0) = 1
def fact(n) = n * fact(n - 1)

def fib(0) = 0
def fib(1) = 1
def fib(n) = fib(n - 1) + fib(n - 2)

// List Operations
// These functions provide basic operations for lists.
def Nil() = Nil()
def Cons(h, t) = Cons(h, t)

def length(Nil()) = 0
def length(Cons(h, t)) = 1 + length(t)

def sum(Nil()) = 0
def sum(Cons(h, t)) = h + sum(t)

def append(Nil(), ys) = ys
def append(Cons(h, t), ys) = Cons(h, append(t, ys))

def reverse(xs) = rev(xs, Nil())
def rev(Nil(), acc) = acc
def rev(Cons(h, t), acc) = rev(t, Cons(h, acc))

def head(Cons(h, t)) = h
def tail(Cons(h, t)) = t
def last(Cons(h, Nil())) = h
def last(Cons(h, t)) = last(t)

// Test Lists
// These lists are used for testing purposes.
def testList() = Cons(1000, Cons(300, Cons(30, Cons(7, Nil()))))
def testList2() = Cons(1000.1337, Cons(300, Cons(30, Cons(7, Nil()))))
```

## Notes
Currently, arithmetic and comparison operators are evaluated natively in Java, not via rewrite rules. Replacing this with a fully rule-based system is a possible future direction.

The project is intended for experimentation and research, especially in understanding the limits and expressiveness of pure term rewriting as a computation model.
