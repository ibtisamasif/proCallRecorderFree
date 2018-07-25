package lvc.pro.com.free.pojo_classes;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * Created by LVC on 22-Aug-17.
 */

public class Contacts implements Comparable {
    int id;
    String name;
    String number;
    Bitmap photo;
    String photoUri;
    String time;
    String date;
    long timestamp;
    int fav;
    int state;
    String records;
    int view;

    public Contacts() {

    }

    public Contacts(int id, String _phone_number, int fav, int state) {
        this.id = id;
        this.number = _phone_number;
        this.fav = fav;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getFav() {
        return fav;
    }

    public void setFav(int fav) {
        this.fav = fav;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }

    public String getRecords() {
        return records;
    }

    public void setRecords(String records) {
        this.records = records;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        int compareage = (int) ((Contacts) o).getTimestamp();
        /* For Ascending order*/
        return (int) (this.timestamp - compareage);

        /* For Descending order do like this */
        //return compareage-this.studentage;
    }

    @Override
    public String toString() {
        return "Contacts{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", photo=" + photo +
                ", photoUri='" + photoUri + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", timestamp=" + timestamp +
                ", fav=" + fav +
                ", state=" + state +
                ", records='" + records + '\'' +
                ", view=" + view +
                '}';
    }
}
