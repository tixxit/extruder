# Extruder

An example of using implicit macros to reduce boilerplate. Read the motivation.

## Motivation

Let's say that we have a bunch of case classes that all have a common property, but don't extend a common trait with this property. For example, `weight: Int`:

```
case class Cat(name: String, weight: Int)
case class Tool(weight: Int)
case class Car(make: String, model: String, weight: Int)
```

We want to be able to write a generic `isHeavy` function that will take any
item (`Cat`, `Tool`, `Car`, etc) and tell us if it is heavy (over 20lbs).  In a
dynamic language, we would just use the property and everything would work.

```scala
def isHeavy[A](item: A): Boolean = item.weight > 20
```

But this won't compile in Scala. In Scala the usual thing to do is to create a
type class that can extract the property from some generic type A, and then to
implement that type class for all `A`s we care about.

```scala
trait HasWeight[A] {
  def apply(item: A): Int
}

object HasWeight {
  def apply[A](f: A => Int): HasWeight[A] =
    new HasWeight[A] {
      def apply(item: A): Int = f(item)
    }

  implicit val CatHasWeight = HasWeight[Cat](_.weight)
  implicit val ToolHasWeight = HasWeight[Tool](_.weight)
  implicit val CarHasWeight = HasWeight[Car](_.weight)
}
```

We can then use this type class in our method:

``scala
def isHeavy[A](item: A)(implicit getWeight: HasWeight[A]): Boolean =
  getWeight(item) > 20

assert(!isHeavy(Cat("Mittens", 16)))
assert(!isHeavy(Tool(4)))
assert(isHeavy(Car("Honda", "Civic", 2600)))
```

This is a bit unsatisfying though; it's a fairly heavy weight solution to a
simple problem. There is lots of boilerplate, extra types and classes you have
to maintain, etc.

Extruder removes a lot of this boilerplate - letting us, essentially, create these kind of type classes for free by using implicit macros. We can just write:

```scala
def isHeavy[A](item: A)(implicit getWeight: Prop.weight.Extruder[A, Int]): Boolean =
  getWeight(item) > 20

assert(!isHeavy(Cat("Mittens", 16)))
assert(!isHeavy(Tool(4)))
assert(isHeavy(Car("Honda", "Civic", 2600)))
```

Boilerplate is all but gone and works for any shallow property, including case class parameters, and any nullary methods.

## Example

```scala
sealed trait Species
object Cat extends Species
object Dog extends Species

case class Person(name: String, age: Int)
case class Pet(name: String, age: Int, species: Species)

def name[A](a: A)(implicit getName: Prop.name.Extruder[A, String]): String =
  getName(a)

def under10[A](a: A)(implicit getAge: Prop.age.Extruder[A, Int]): Boolean =
  getAge(a) < 10

def isCat[A](animal: A)(implicit getSpecies: Prop.species.Extruder[A, Species]): Boolean =
  getSpecies(animal) == Cat

val alice = Person("Alice", 23)
val boots = Pet("boots", 8, Cat)

println("alice.name           : " + name(alice))
println("boots.name           : " + name(boots))
println("alice.age < 10       : " + under10(alice))
println("boots.age < 10       : " + under10(boots))
// println(isCat(alice)) -- WON'T COMPILE!
println("boots.species == cat : " + isCat(boots))
```
