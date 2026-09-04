package tourGuide;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class TestPerformance {

	/*
	 * A note on performance improvements:
	 *
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *
	 *
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent.
	 *
	 *     These are performance metrics that we are trying to hit:
	 *
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	//@Ignore
	@Test
	public void highVolumeTrackLocation() throws InterruptedException {
		// Set up the GPS utility used to simulate user location tracking
		GpsUtil gpsUtil = new GpsUtil();
		// Set up the rewards service, backed by the external RewardCentral provider
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Configure the test to generate 100,000 internal test users
		//  should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100000);

		// Main service under test: handles user tracking and reward calculation
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		// Retrieve the full list of generated test users
		List<User> allUsers = tourGuideService.getAllUsers();

		// Thread pool used to process users concurrently instead of sequentially,
		// since sequential tracking of 100,000 users would take far too long
		ExecutorService executor = Executors.newFixedThreadPool(200);

		// Timer used to measure the total execution time
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		// Submit one asynchronous tracking task per user to the thread pool;
		// each task calls trackUserLocation independently and in parallel
		List<CompletableFuture<Void>> futures = allUsers.stream()
				.map(user -> CompletableFuture.runAsync(
						() -> tourGuideService.trackUserLocation(user), executor))
				.collect(Collectors.toList());

		// Wait for all tracking tasks to complete before measuring the elapsed time
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		// Stop the timer once every user has been tracked
		stopWatch.stop();
		// Release the thread pool's resources now that all tasks are done
		executor.shutdown();
		// Stop the background location tracker so the test can terminate cleanly
		tourGuideService.tracker.stopTracking();

		// Log the total elapsed time in seconds for visibility
		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

		// Assert that the total time stayed within the 15-minute requirement
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	//@Ignore
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		// Stop the background tracker immediately: we don't need continuous tracking here,
		// and letting it run would concurrently modify each user's visited locations
		// while calculateRewards() reads them on other threads, causing a ConcurrentModificationException
		tourGuideService.tracker.stopTracking();

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		ExecutorService executor = Executors.newFixedThreadPool(200);
		List<CompletableFuture<Void>> futures = allUsers.stream()
				.map(u -> CompletableFuture.runAsync(() -> rewardsService.calculateRewards(u), executor))
				.collect(Collectors.toList());

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		executor.shutdown();

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
}
	

