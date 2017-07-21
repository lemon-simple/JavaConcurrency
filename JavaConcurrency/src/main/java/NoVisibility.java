import java.util.concurrent.TimeUnit;

public class NoVisibility {
        static boolean isRunning = true;
        public static void main(String[] args) throws InterruptedException {
            Thread runningT = getRunningThread();
            runningT.start();
            TimeUnit.SECONDS.sleep(10);
            isRunning = false;//注意： main Thread 执行到此,预期runningThread 应该结束
        }
        public static Thread getRunningThread() {
            return new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                    }
                }
            }, "RunningThread");
        }
    }