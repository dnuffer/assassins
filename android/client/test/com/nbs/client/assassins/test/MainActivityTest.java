package com.nbs.client.assassins.test;

import com.nbs.client.assassins.R;
import com.nbs.client.assassins.controllers.MainActivity_;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.Robolectric;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
	@Test
	public void shouldHaveTitle() throws Exception {
		String title = Robolectric.buildActivity(MainActivity_.class).create().get().getSupportActionBar().getTitle().toString();
		assertThat(title, equalTo("assassins"));
		
		//http://blog.javabien.net/2009/06/21/mockitos-partial-mocks-testing-real-objects-just-got-easier/
		//FileTemplate mockFileTemplate = mock(FileTemplate.class);
		//CopyFileRule rule = spy(new CopyFileRule("src.txt", "dest.txt"));
		//doReturn(mockFileTemplate).when(rule).createFileTemplate();
	}

}
