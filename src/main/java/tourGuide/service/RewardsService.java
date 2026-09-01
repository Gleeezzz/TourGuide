package tourGuide.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
	// Conversion factor: 1 nautical mile = 1.15077945 statute miles
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	// Default proximity buffer, used as a fallback/reset value
	private int defaultProximityBuffer = 10;
	// Current proximity buffer, can be overridden via setProximityBuffer()
	private int proximityBuffer = defaultProximityBuffer;
	// Maximum distance (in miles) within which a user is considered "at" an attraction
	private int attractionProximityRange = 200;
	// Reference to the GPS utility service, used to fetch attraction data
	private final GpsUtil gpsUtil;
	// Reference to the external reward points provider
	private final RewardCentral rewardsCentral;
	// Cached list of all attractions, fetched once at construction time
	// to avoid repeated (and potentially slow) calls to gpsUtil.getAttractions()
	private final List<Attraction> attractions;

	// Constructor: injects dependencies and pre-loads the attractions list once
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
		this.attractions = gpsUtil.getAttractions(); // fetched once, reused for every user
	}

	// Overrides the default proximity buffer with a custom value
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	// Resets the proximity buffer back to its default value
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	// Calculates and assigns rewards for a given user based on their visited locations
	public void calculateRewards(User user) {
		// Get all locations the user has visited so far
		List<VisitedLocation> userLocations = user.getVisitedLocations();

		// Build a lookup set of attraction names the user has already been rewarded for,
		// computed once per call instead of re-scanning the reward list on every iteration
		Set<String> rewardedAttractions = user.getUserRewards().stream()
				.map(r -> r.attraction.attractionName)
				.collect(Collectors.toSet());

		// For each visited location, check against every known attraction
		for (VisitedLocation visitedLocation : userLocations) {
			for (Attraction attraction : attractions) {
				// Skip attractions already rewarded, and only reward if the user was near it
				if (!rewardedAttractions.contains(attraction.attractionName)
						&& nearAttraction(visitedLocation, attraction)) {
					// Grant the reward: store the visited location, attraction, and points earned
					user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					// Mark this attraction as rewarded so it isn't processed again for this user
					rewardedAttractions.add(attraction.attractionName);
				}
			}
		}
	}

	// Checks whether a given location is within the general "attraction proximity range"
	// (used for filtering nearby attractions in the UI, independent of user-specific rewards)
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	// Checks whether a visited location is close enough to an attraction
	// to count as the user having "visited" that attraction (uses the reward proximity buffer)
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	// Fetches the reward points for a given attraction/user pair from the external RewardCentral service
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	// Computes the great-circle distance (in statute miles) between two geographic locations
	// using the spherical law of cosines
	public double getDistance(Location loc1, Location loc2) {
		// Convert degrees to radians for trigonometric functions
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		// Angular distance between the two points, in radians
		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
				+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		// Convert angle to nautical miles (1 degree of arc ≈ 60 nautical miles)
		double nauticalMiles = 60 * Math.toDegrees(angle);
		// Convert nautical miles to statute miles
		double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
		return statuteMiles;
	}
}