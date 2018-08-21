// Define class Teacher, subclass of Person
public class Teacher extends Person {
   // Instance variables
   private int numCourses;   // number of courses taught currently
   private String[] courses; // course codes
   private static final int MAX_COURSES = 10; // maximum courses
   
   // Constructor
   public Teacher(String name, String address) {
      super(name, address);
      numCourses = 0;
      courses = new String[MAX_COURSES];
   }
   
   @Override
   public String toString() {
      return "Teacher: " + super.toString();
   }
   
   // Return false if duplicate course to be added
   public boolean addCourse(String course) {
      // Check if the course already in the course list
      for (int i = 0; i < numCourses; i++) {
         if (courses[i].equals(course)) return false;
      }
      courses[numCourses] = course;
      numCourses++;
      return true;
   }
   
   // Return false if the course does not in the course list
   public boolean removeCourse(String course) {
      // Look for the course index
      int courseIndex = numCourses;
      for (int i = 0; i < numCourses; i++) {
         if (courses[i].equals(course)) {
            courseIndex = i;
            break;
         }
      }
      if (courseIndex == numCourses) { // cannot find the course to be removed
         return false;   
      } else {  // remove the course and re-arrange for courses array
         for (int i = courseIndex; i < numCourses-1; i++) {
            courses[i] = courses[i+1];
         }
         numCourses--;
         return true;
      }
   }
}