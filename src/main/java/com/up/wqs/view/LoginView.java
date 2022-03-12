
package com.up.wqs.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;

import com.up.wqs.constant.AppConstants;

/**
 * 模块说明： 登录界面
 * 
 */
public class LoginView extends JFrame {

	private static final long serialVersionUID = -5278598737087831336L;
	private JPanel jPanelCenter, jPanelSouth;
	private JTextField username;
	private JPasswordField password;
	private JButton loginButton, resetButton;

	public LoginView() {
		init();
	}

	private void init() {
		this.setTitle("登陆");

		jPanelCenter = new JPanel();
		jPanelCenter.setPreferredSize(new Dimension(100, 0));
		jPanelCenter.setLayout(new GridLayout(3, 2));
		jPanelCenter.add(new JLabel(AppConstants.LOGIN_USERNAME));
		username = new JTextField();
		jPanelCenter.add(username);
		jPanelCenter.add(new JLabel(AppConstants.LOGIN_PASSWORD));
		password = new JPasswordField();
		// enter key listener
		password.addKeyListener(new LoginListener());
		jPanelCenter.add(password);


		jPanelCenter.add(new JLabel("软件使用以及自媒体定制"));
		jPanelCenter.add(new JLabel("请联系：304649190"));

		jPanelSouth = new JPanel();

		jPanelSouth.setLayout(new GridLayout(1, 2));
		loginButton = new JButton(AppConstants.LOGIN);
		loginButton.setBounds(20, 230, 150, 60);
		loginButton.addActionListener(e -> check());

		jPanelSouth.add(loginButton);
		resetButton = new JButton(AppConstants.RESET);
		resetButton.addActionListener(e -> {
			username.setText("");
			password.setText("");
		});
		jPanelSouth.add(resetButton);

		this.add(jPanelCenter, BorderLayout.CENTER);
		this.add(jPanelSouth, BorderLayout.SOUTH);

		this.setBounds(450, 250, 375, 180);
		this.setResizable(false);
		this.setVisible(true);
	}

	private class LoginListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				check();
			}
		}
	}

	private void check() {
		if (username.getText().equals("admin") && password.getText().equals( "123456" )) {
			dispose();
			new MainView();
		} else {
			username.setText("");
			password.setText("");
			JOptionPane.showMessageDialog(null, "账号或密码错误!");
		}
	}

}
