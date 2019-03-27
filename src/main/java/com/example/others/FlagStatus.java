package com.example.others;

public enum FlagStatus {
	READY {
		public String toString() {
			return "READY";
		}
	},
	WAIT {
		public String toString() {
			return "WAIT";
		}
	},
	INIT {
		public String toString() {
			return "INIT";
		}
	},
	PROCESSED {
		public String toString() {
			return "PROCESSED";
		}
	}
}
