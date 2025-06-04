# RX – A Term-Rewriting Language

RX (**R**ewrite e**X**pressions) is a small interpreter for a custom term-rewriting language. It is designed to explore how fundamental programming concepts can be represented and executed via rule-based rewriting of expressions.

The goal is not to build a production-ready language, but to study and experiment with how term rewriting interacts with typical features of programming languages such as control flow, recursion, and evaluation strategies.

## Features

- ✅ User-defined rewrite rules with pattern matching
- ✅ Support for integer and boolean literals - more to follow
- ✅ Arithmetic operations: `+`, `-`, `*`, `/`
- ✅ Boolean comparisons: `==`, `!=`, `<`, `<=`, `>`, `>=`
- ✅ Conditional branching via `if`
- ✅ Separation of rules (definitions) and expressions (to be evaluated)
- ✅ Native evaluation of arithmetic and boolean operators (to be possibly replaced in the future)

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
// Control-Structures
def if(true, thenBranch, elseBranch) = thenBranch
def if(false, thenBranch, elseBranch) = elseBranch


def max(a, b) = if(a > b, a, b)
def min(a, b) = if(a < b, a, b)


//Advanced mathematical operations
def square(x) = x * x

def fact(0) = 1
def fact(n) = n * fact(n - 1)

def fib(0) = 0
def fib(1) = 1
def fib(n) = fib(n - 1) + fib(n - 2)



def abs(x) = if(x < 0, 0 - x, x)

def clamp(x, minVal, maxVal) = max(min(x, maxVal), minVal)

def isZero(x) = x == 0
def isPositive(x) = x > 0
def isNegative(x) = x < 0

```

## Notes
Currently, arithmetic and comparison operators are evaluated natively in Java, not via rewrite rules. Replacing this with a fully rule-based system is a possible future direction.

The project is intended for experimentation and research, especially in understanding the limits and expressiveness of pure term rewriting as a computation model.