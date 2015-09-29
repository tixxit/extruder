package net.tixxit.extruder

object Demo extends App {
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
}
