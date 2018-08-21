// Define Student class, subclass of Person
public class Student extends Person {
   // Instance variables
   private int numCourses;   // number of courses taken so far, max 30 
   private String[] courses; // course codes
   private int[] grades;     // grade for the corresponding course codes
   private static final int MAX_COURSES = 30; // maximum number of courses
   
   // Constructor
   public Student(String name, String address) {
      super(name, address);
      numCourses = 0;
      courses = new String[MAX_COURSES];
      grades = new int[MAX_COURSES];
   }
   
   @Override
   public String toString() {
      return "Student: " + super.toString();
   }
   
   // Add a course and its grade - No validation in this method 
   public void addCourseGrade(String course, int grade) {
      courses[numCourses] = course;
      grades[numCourses] = grade;
      ++numCourses;
   }
   
   // Print all courses taken and their grade
   public void printGrades() {
      System.out.print(this);
      for (int i = 0; i < numCourses; ++i) {
         System.out.print(" " + courses[i] + ":" + grades[i]);
      }
      System.out.println();
   }
   
   // Compute the average grade
   public double getAverageGrade() {
      int sum = 0;
      for (int i = 0; i < numCourses; i++ ) {
         sum += grades[i];
      }
      return (double)sum/numCourses;
   }
}