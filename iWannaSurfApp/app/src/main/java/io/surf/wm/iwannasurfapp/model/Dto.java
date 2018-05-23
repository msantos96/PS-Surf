package io.surf.wm.iwannasurfapp.model;

class Dto {
    public class Spot {
        public DbSpot dbSpot;
        public RealTimeData realTimeData;
    }
    public class DbSpot {
        public String _id;
        public Float rating;
        public Identification identification;
        public SwellInterval swell;
        public WindInterval wind;
    }
    class Identification {
        public Double lat;
        public Double lon;
        public String name;
        public Double distance;
        public AdditionalInfo additionalInfo;
    }
    class AdditionalInfo {
        public Crowd crowd;
        public Double realLat;
        public Double realLong;
        public Double thumbsUp;
        public Double thumbsDown;
        public Integer rank;
    }
    class Crowd {
        public Integer weekDays;
        public Integer weekEnds;
    }
    class SwellInterval {
        public Interval<Double> height;
        public Interval<Double> period;
        public Interval<Integer> direction;
        public String[] compassDirection;
    }
    class WindInterval {
        public Interval<Integer> speed;
        public Interval<Integer> direction;
        public String[] compassDirection;
    }
    class Interval<T> {
        public T min;
        public T max;
    }

    class RealTimeData{
        public Swell swell;
        public Wind wind;
    }
    class Swell {
        public Double height;
        public Double period;
        public Double direction;
    }
    class Wind {
        public Double speed;
        public Double direction;
    }
}