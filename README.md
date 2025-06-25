# RX – A Term-Rewriting Language

RX (**R**ewrite e**X**pressions) is a small interpreter for a custom term-rewriting language. It is designed to explore how fundamental programming concepts can be represented and executed via rule-based rewriting of expressions.

The goal is not to build a production-ready language, but to study and experiment with how term rewriting interacts with typical features of programming languages such as control flow, recursion, and evaluation strategies.

## Features

- ✅ User-defined rewrite rules with pattern matching
- ✅ Support for integer, float and boolean literals - more to follow
- ✅ Arithmetic operations: `+`, `-`, `*`, `/`
- ✅ Boolean comparisons: `==`, `!=`, `<`, `<=`, `>`, `>=`
- ✅ Conditional branching via `if`
- ✅ Separation of rules (definitions) and expressions (to be evaluated)
- ✅ Native evaluation of arithmetic and boolean operators (to be possibly replaced in the future)
- ✅ REPL for testing the evaluation of expressions and adding of new rules

## Example Programs
### 1. Conditional Expressions
```rx
def if(true, thenBranch, elseBranch) = thenBranch
def if(false, thenBranch, elseBranch) = elseBranch

if(5 < 10, 1, 0)
```
**Result:** 1

### 2. Maximum and Minimum
```rx
def if(true, thenBranch, elseBranch) = thenBranch
def if(false, thenBranch, elseBranch) = elseBranch

def max(a, b) = if(a > b, a, b)
def min(a, b) = if(a < b, a, b)

max(5, 12)
min(5, 12)
```
**Result of max(5, 12):** 12\
**Result of min(5, 12):** 5

### 3. Recursion
```rx
def factorial(0) = 1
def factorial(n) = n * factorial(n - 1)

factorial(5)
```
**Result:** 120

### 4. Simple Arithmetic Function
```rx
def square(x) = x * x

square(4)
```
**Result:** 16

## Prelude
A set of basic definitions is loaded before each program:
```rx
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

## Planned or possible extensions
- let bindings
- Strings and more data types
- Input/output primitives (with controlled side effects)
- Type system or type hints
- Rewriting traces and visual debugging tools
- Modules and imports for larger programs

## Notes
Currently, arithmetic and comparison operators are evaluated natively in Java, not via rewrite rules. Replacing this with a fully rule-based system is a possible future direction.

The project is intended for experimentation and research, especially in understanding the limits and expressiveness of pure term rewriting as a computation model.
