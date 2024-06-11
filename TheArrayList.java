
public class TheArrayList <E> {
    /*
     * Prinsip Encapsulation dari OOP:
     * 1. semua variabel dari class HARUS private
     *    (hanya dapat diakses oleh classnya sendiri)
     * 2. akses (set dan get) variabel melalui public method
     *    yang dibuat di class ini.
     */
    Object[] thelist;
    private int n;
    private int max_size;

    //Constructor
    public TheArrayList(int max_size) {
        thelist = new Object[max_size];
        n = 0;
        this.max_size = max_size;
    }
    //mengembalikan ukuran maksimum array
    public int maxSize() {
        return max_size;
    }
    //mengembalikan jumlah elemen array
    //saat ini
    public int size() {
        return n;
    }

    private boolean isFull() {
        if(n == max_size) return true;
        else return false;
    }

    public boolean isEmpty() {
        if(n == 0) return true;
        else return false;
    }

    public void add(E value) {
        if(!isFull()) {
            thelist[n] = value;
            n = n + 1;
        }
        else {
            System.out.println("List sudah penuh!");
        }
    }

    public void add(int index, E value) {
        if(index >= 0 && !isFull()) {
            n = n + 1;
            int i = n;
            do {
                thelist[i] = thelist[i-1];
                i = i - 1;
            }while(i > index);
            thelist[index] = value;
        }
        else {
            System.out.println("List sudah penuh!");
        }
    }

    public void remove(int index) {
        if(index >= 0 && !isEmpty()) {
            for(int i = index; i < n-1; i++) 
                thelist[i] = thelist[i+1];
            thelist[n-1] = null;
            n = n - 1;
        }
    }

    public E get(int i) {
        @SuppressWarnings("unchecked")
        final E e = (E) thelist[i];
        return e;
    }

    public void set(int index, E value) {
        thelist[index] = value;
    }

    public void clear() {
        if(!isEmpty()) {
            for(int i = 0; i < n; i++) 
                thelist[i] = null;    
            n = 0;
        }
    }

    public void cetakList() {
        //jika list kosong, tampilkan pesan list kosong
		if(isEmpty()) System.out.println("List kosong!");
        // jika list tidak kosong, maka cetak elemen pada list
		else {
            System.out.print("[ ");
            for(int i = 0; i < n; i++) {
				System.out.print(thelist[i].toString() + " ");
			}
            System.out.println("]");
		}
    }
}
