/*
 * Restaurant Booking System: example code to accompany
 *
 * "Practical Object-oriented Design with UML"
 * Mark Priestley
 * McGraw-Hill (2004)
 */

package booksys.application.domain ;

import java.sql.Date ;
import java.sql.Time ;
import java.util.* ;

public class BookingSystem
{
  // Attributes:

  Date currentDate ;
  Date today ;
  
  // Associations:

  Restaurant restaurant = null ;
  Vector currentBookings ;
  Booking selectedBooking ;

  // Singleton:
  
  private static BookingSystem uniqueInstance ;

  public static BookingSystem getInstance()
  {
    if (uniqueInstance == null) {
      uniqueInstance = new BookingSystem() ;
    }
    return uniqueInstance ;
  }

  private BookingSystem()
  {
    today = new Date(Calendar.getInstance().getTimeInMillis()) ;
    restaurant = new Restaurant() ;
  }

  // Observer: this is `Subject/ConcreteSubject'

  Vector observers = new Vector() ;

  public void addObserver(BookingObserver o)
  {
    observers.addElement(o) ;
  }
  
  public void notifyObservers()
  {
    Enumeration enumV = observers.elements() ;
    while (enumV.hasMoreElements()) {
      BookingObserver bo = (BookingObserver) enumV.nextElement() ;
      bo.update() ;
    }
  }

  public boolean observerMessage(String message, boolean confirm)
  {
    BookingObserver bo = (BookingObserver) observers.elementAt(0) ;
    return bo.message(message, confirm) ;
  }
  
  // System messages:

  public void display(Date date)
  {
    currentDate = date ;
    currentBookings = restaurant.getBookings(currentDate) ;
    selectedBooking = null ;
    notifyObservers() ;
  }
  
  public void makeReservation(int covers, Date date, Time time, int tno,
			      String name, String phone)
  {
    tno = autoAssignTable(covers, time, tno);
    if (!overflow(tno, covers)) {
      Booking b
	= restaurant.makeReservation(covers, date, time, tno, name, phone) ;
      currentBookings.addElement(b) ;
      notifyObservers() ;
    }
  }
 
  public void makeWalkIn(int covers, Date date, Time time, int tno)
  {
    tno = autoAssignTable(covers, time, tno);
    if (!overflow(tno, covers)) {
      Booking b = restaurant.makeWalkIn(covers, date, time, tno) ;
      currentBookings.addElement(b) ;
      notifyObservers() ;
    }
  }

  private int autoAssignTable(int covers, Time time, int tno) {
    int TableID = 0;
    if (covers <= 2) {
      for (int i=1;i<11;i++) {
        if (!Booked(time, i, null) && TableID == 0) {
          TableID = i;
        }
      }
    }
    else {
      for (int i=5;i<11;i++) {
        if (!Booked(time, i, null) && TableID == 0) {
          TableID = i;
        }
      }
    }
      return TableID;
  }

  private boolean Booked(Time startTime, int tno, Booking ignore)
  {
    boolean doubleBooked = false ;

    Time endTime = (Time) startTime.clone() ;
    endTime.setHours(endTime.getHours() + 2) ;

    Enumeration enumV = currentBookings.elements() ;
    while (!doubleBooked && enumV.hasMoreElements()) {
      Booking b = (Booking) enumV.nextElement() ;
      if (b != ignore && b.getTableNumber() == tno
              && startTime.before(b.getEndTime())
              && endTime.after(b.getTime())) {
        doubleBooked = true ;
      }
    }
    return doubleBooked ;
  }


  public void selectBooking(int tno, Time time)
  {
    selectedBooking = null ;
    Enumeration enumV = currentBookings.elements() ;
    while (enumV.hasMoreElements()) {
      Booking b = (Booking) enumV.nextElement() ;
      if (b.getTableNumber() == tno) {
	if (b.getTime().before(time)
	    && b.getEndTime().after(time)) {
	  selectedBooking = b ;
	}
      }
    }
    notifyObservers() ;
  }

  public void cancel()
  {
    if (selectedBooking != null) {
      if (observerMessage("Are you sure?", true)) {
	currentBookings.remove(selectedBooking) ;
	restaurant.removeBooking(selectedBooking) ;
	selectedBooking = null ;
	notifyObservers() ;
      }
    }
  }
  
  public void recordArrival(Time time)
  {
    if (selectedBooking != null) {
      if (selectedBooking.getArrivalTime() != null) {
	observerMessage("Arrival already recorded", false) ;
      }
      else {
	selectedBooking.setArrivalTime(time) ;
	restaurant.updateBooking(selectedBooking) ;
	notifyObservers() ;
      }
    }
  }

  public void transfer(Time time, int tno)
  {
    if (selectedBooking != null) {
      if (selectedBooking.getTableNumber() != tno) {
	if (!doubleBooked(selectedBooking.getTime(), tno, selectedBooking)
	    && !overflow(tno, selectedBooking.getCovers())) {
	  selectedBooking.setTable(restaurant.getTable(tno)) ;
	  restaurant.updateBooking(selectedBooking) ;
	}
      }
      notifyObservers() ;
    }
  }

  private boolean doubleBooked(Time startTime, int tno, Booking ignore)
  {
    boolean doubleBooked = false ;

    Time endTime = (Time) startTime.clone() ;
    endTime.setHours(endTime.getHours() + 2) ;
    
    Enumeration enumV = currentBookings.elements() ;
    while (!doubleBooked && enumV.hasMoreElements()) {
      Booking b = (Booking) enumV.nextElement() ;
      if (b != ignore && b.getTableNumber() == tno
	  && startTime.before(b.getEndTime())
	  && endTime.after(b.getTime())) {
	doubleBooked = true ;
	observerMessage("Double booking!", false) ;
      }
    }
    return doubleBooked ;
  }
  
  private boolean overflow(int tno, int covers)
  {
    boolean overflow = false ;
    Table t = restaurant.getTable(tno) ;
      
    if (t.getPlaces() < covers) {
      overflow = !observerMessage("Ok to overfill table?", true) ;
    }
    
    return overflow ;
  }
  
  // Other Operations:

  public Date getCurrentDate()
  {
    return currentDate ;
  }
  
  public Enumeration getBookings()
  {
    return currentBookings.elements() ;
  }

  public Booking getSelectedBooking()
  {
    return selectedBooking ;
  }

  public static Vector getTableNumbers()
  {
    return Restaurant.getTableNumbers() ;
  }
}
