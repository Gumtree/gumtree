#Import required modules
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

#Define the name of the data directory, and define the header names for the data (initial energy, singal intensity, and error).
folder_name = "Norm_data_all/"
head_names = ["Ei", "signal", "sigma"]

#Make a function which imports the data.
def import_fn(f1, f2, f3):
    df1 = pd.read_csv(folder_name+f1, skiprows=19, delim_whitespace=True, header=None, names = head_names) #Each measurement at a certain temp for a certain mono is made up of 3 scans of different  
    df2 = pd.read_csv(folder_name+f2, skiprows=19, delim_whitespace=True, header=None, names = head_names) #energy ranges and energy steps. So 3 files must be loaded in.
    df3 = pd.read_csv(folder_name+f3, skiprows=19, delim_whitespace=True, header=None, names = head_names) #Preamble of the files are skipped; data begins at line 20 (so I skip first 19 lines).
    df = pd.concat([df1,df2,df3])                                                                          #The 3 datasets are concatenated into a single dataframe.
    return df.iloc[::-1].sort_values('Ei').reset_index(drop='True')                                             # Since the data is taken from high to low energy, 
                                                                                                                # I reverse the order of the dataframe to be in increasing energy order. Then reset index 
                                                                                                                # to match new order.


#Now I import the files associated with a particular measurement.
SMPL_PG = import_fn("95661.xys", "95662.xys", "95663.xys")
SMPL_Cu = import_fn("95665.xys", "95666.xys", "95667.xys")

#Of course, the associated background scans must be loaded in as well.
EMPTY_PG = import_fn("95684.xys", "95679.xys", "95680.xys")
EMPTY_Cu = import_fn("95681.xys", "95682.xys", "95683.xys")



#NAME YOUR SAMPLE HERE!:
Sample_name = 'LA'     #Name the sample
Sample_temp = 'test'    #Temp

#Let's give some names to our wonderful measurements; they've earnt it.
SMPL_PG.name = Sample_name + Sample_temp + '_PG'
SMPL_Cu.name = Sample_name + Sample_temp + '_Cu'

'''
Rescale background scans to match sample scans:

If mismatched energy steps between background and sample measurements is an issue, the cell below is formatted to solve this issue
'''

#The background scans and the sample scans below 26.81 meV doe not match in energy steps (...oops). So here, I upsample the background scan to be the same length as the sample scan over this region.
#If this is not an issue with the given dataset, this cell can be skipped.

PG_LOW = 26.82 #Define the threshold (in energy) for the last PG scans.


#Now let's rescale (via linear interpolation) the signal and the sigma of the empty low E scan to match the energy steps of the sample scan. 
int_signal = np.interp(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,0][EMPTY_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,1][EMPTY_PG['Ei'] < PG_LOW])
int_err = np.interp(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,0][EMPTY_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,2][EMPTY_PG['Ei'] < PG_LOW])

#Make a new dataframe for the empty scan where the low E data is replaced with the rescaled data.
EMPTY_PG_interp = pd.concat([EMPTY_PG[EMPTY_PG['Ei'] > PG_LOW], 
                  pd.DataFrame(np.transpose(np.array([SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], int_signal, int_err])),
                  columns=head_names)]).sort_values('Ei').reset_index(drop='True')

EMPTY_PG = EMPTY_PG_interp #Set the interpolated dataset as the main exmpty scan to be used through the rest of the processing

'''
Energy correction:

Here we set up for the energy correction
'''

#Define the variables for calculations bewteen Ei and dE and deltaE
h = 6.626E-34           #kg m2 s-2 s
m_n = 1.674929E-27      #kg
e = 1.60217662E-19      #C
k = 1.38064852E-23      #m2 kg s-2 K-1
dE_f = 0.0005           #eV
Ef = 0.0012             #eV
dtheta = np.radians(0.25)   #rad
T = 293                 #K
d_PG002 = 3.354E-10     #m
d_Cu200 = 1.807E-10     #m

#Function which converts Ei to dE
def Ei_to_DeltaE(Ei,d):
    
    Lambda = np.sqrt(h**2/(2*m_n*(Ei*e/1000)))      #Calculates Lambda in m
    theta = np.arcsin(Lambda*1/(2*d))               #Calculates scattering angle theta in radians
    dLambda = Lambda*1/(np.tan(theta))*dtheta       #Calculates dLamnda in m
    dEi = -h**2/(2*m_n)*dLambda/(Lambda**3)/e       #Calculates dEi in eV
    return pd.DataFrame(np.sqrt(dEi**2 + dE_f**2)*1000)           #Calculates dE in meV
    

#Make dataframes for dE to be later used for energy correction. This is done for both the PG and Cu monos.
dE_PG = Ei_to_DeltaE(SMPL_PG['Ei'],d_PG002).rename(columns = {'Ei': 'dE'}) 
dE_Cu = Ei_to_DeltaE(SMPL_Cu['Ei'],d_Cu200).rename(columns = {'Ei': 'dE'})  #We simply execute the function above and relabel the column as 'dE'.

#While we're here, let's make dataframes for DeltaE (energy transfer). This is what we will finally plot as our independent var.
SMPL_PG['DeltaE'] = SMPL_PG['Ei'] - 1000*Ef
SMPL_Cu['DeltaE'] = SMPL_Cu['Ei'] - 1000*Ef #Here we simply compute Ei-Ef for PG and Cu measurements.
'''
Setting up the energy correction and background subtraction:
'''
#Exponential correction function paramters:
A = 9.64047
t = 4.94769
y0 = 1.05681

#Background subtraction and signal correction from energy uncertainty, and higher-order exponential correction function
def bkg_subtraction(samp_data, bkg_data, dE_data):
    samp_data['signal_bkg_sub'] = samp_data['signal'] - bkg_data['signal']                                              #Background subraction
    samp_data['signal_ECorr'] = (samp_data['signal_bkg_sub'] / dE_data['dE']) / (A*np.exp(-samp_data['Ei']/t) + y0)     #Exponential correction
    samp_data['Fin_Lam_Corr'] = samp_data['signal_ECorr']                                                                   #Here, we are just setting up a new column from which 
                                                                                                                            #we will subract the high-order scattering contamination
'''
Wavelength contamination removal:

Excecute the background subtraction and exponential function correction, then set up and execute the wavelength contamination removal
'''
#We now apply the corrections from the funciton above to both the PG and Cu measurements:
bkg_subtraction(SMPL_PG, EMPTY_PG, dE_PG)
bkg_subtraction(SMPL_Cu, EMPTY_Cu, dE_Cu)

#####------------------------------------------------------------------------------------------------------------------------------------------------#####
#Wavelength contamination removal:

#Make a function which corrects the higher order scattering.
#This is the main reason I automated the process in the first place. The energy steps at the higher energies (supplying the corrections)
#are not necessarily the same as the lower enery steps. So once the correction has been generated, it must be rescaled and interpolated
#to match the energy steps of the actual data being corrected. This is what the function below does.
#Note that mod should be set to 1, unless some of the corrected intensity drops below 0, then adjust as needed to keep data above 0.

def higher_order_corr(Data_to_correct, correcting_data, scatter_order, mod):    #We input the data to correct, and a separate correcting data so tha Cu can correct PG

    corr_name = 'lam_'+str(scatter_order)+'_corr_from_'+correcting_data.name    #Define the name of the data-to-correct, including the order of scattering correction
                                                                                #to track the correction being made

    Data_to_correct[corr_name] = pd.DataFrame(np.interp(Data_to_correct['Ei'][Data_to_correct['Ei'] < correcting_data['Ei'].max() / scatter_order**2],  
    correcting_data['Ei'] / scatter_order**2,                                                                                                               
    correcting_data['signal_ECorr'] / scatter_order**2 / mod))
    #This above is the meaty part. A new column in the sample dataframe is made which includes higher-energy data which is modified in intensity to correct
    #higher-order scattering effects at the lower energies. Thus, this data is also scaled and interpolated to match the energy steps at the lower-energy
    #of the data we are correcting, so that the correction actually works. 
                                                                                    
    Data_to_correct[corr_name] = Data_to_correct[corr_name].fillna(0)                                   #This line changes NaNs (values above the E/n threshold) to 0s 
    Data_to_correct['Fin_Lam_Corr'] = Data_to_correct['Fin_Lam_Corr'] - Data_to_correct[corr_name]      #Final correction by subracting the correction data from the final intensity



#Now we apply the function to all the necessary data. One will have to manually check the highest order of scattering 
#which can be corrected based on the energy range of the scans:

#Self-corrections:
higher_order_corr(SMPL_PG, SMPL_PG, 2, 1)
higher_order_corr(SMPL_Cu, SMPL_Cu, 2, 1)

#Cu correcting PG:
higher_order_corr(SMPL_PG, SMPL_Cu, 2, 1.4)
higher_order_corr(SMPL_PG, SMPL_Cu, 3, 1.4)



#Some lines to check the outputs:

####---This is how you reorder the labels for the DataFrame if desired:---####
#SMPL_PG=SMPL_PG[['DeltaE','Ei','signal','sigma','signal_bkg_sub','signal_ECorr','lam_2_corr_from_LA4K_PG','lam_2_corr_from_LA4K_Cu','lam_3_corr_from_LA4K_Cu','Fin_Lam_Corr']]

####---You can check the DataFrames to make sure they look ok, and if modifer is appropriate:---####
#SMPL_PG
#SMPL_Cu

####---You can check the DataFrames without row cutoff:---####
#with pd.option_context('display.max_rows', None, 'display.max_columns', None,'display.precision', 5,):
#    print(SMPL_PG)
'''
Error bars!
'''
#Can't forget the error bars! (Even though they're not that significant here)

#For the PG
SMPL_PG['signal_bkg_sub_SIGMA'] = np.sqrt(SMPL_PG['sigma']**2 + EMPTY_PG['sigma']**2)
SMPL_PG['Fin_Lam_Corr_SIGMA'] = SMPL_PG['Fin_Lam_Corr'] * (SMPL_PG['signal_bkg_sub_SIGMA'] / SMPL_PG['signal_bkg_sub'])

#For the Cu
SMPL_Cu['signal_bkg_sub_SIGMA'] = np.sqrt(SMPL_Cu['sigma']**2 + EMPTY_Cu['sigma']**2)
SMPL_Cu['Fin_Lam_Corr_SIGMA'] = SMPL_Cu['Fin_Lam_Corr'] * (SMPL_Cu['signal_bkg_sub_SIGMA'] / SMPL_Cu['signal_bkg_sub'])
'''
Exporting the data:
'''
#Let's export the data to csv so we can plot it wherever we want

#Folder in which to save data
save_folder = 'Processed_Data/'

#For PG data
SMPL_PG.to_csv(save_folder+SMPL_PG.name+'_DATA.csv', sep = ',')
#For Cu data
SMPL_Cu.to_csv(save_folder+SMPL_Cu.name+'_DATA.csv', sep = ',')








'''
Below is plotting to check the processing:
'''
# %matplotlib tk
plt.errorbar(SMPL_Cu['DeltaE'], SMPL_Cu['Fin_Lam_Corr'], yerr = SMPL_Cu['Fin_Lam_Corr_SIGMA'], color = 'r')
plt.errorbar(SMPL_PG['DeltaE'], SMPL_PG['Fin_Lam_Corr'], yerr = SMPL_PG['Fin_Lam_Corr_SIGMA'], color = 'black')
plt.plot(SMPL_Cu['DeltaE'], SMPL_Cu['Fin_Lam_Corr'], '-o', markersize=4, label = 'Cu', color = 'r')
plt.plot(SMPL_PG['DeltaE'], SMPL_PG['Fin_Lam_Corr'], '-o', markersize=4,  label = 'PG', color = 'black')
plt.plot(SMPL_PG['DeltaE'], SMPL_PG['signal'], '-o', markersize=4,  label = 'PG-unproc.', color = 'b')
plt.plot(SMPL_PG['DeltaE'], EMPTY_PG['signal']*6, '-o', markersize=4,  label = 'PG-empty.', color = 'g')
plt.legend()
plt.xlim([8.9,70])
plt.ylim([40000,285000])
plt.xlabel("Energy (meV)")
plt.ylabel('Intensity (arb.u.)')


# plt.plot(SMPL_PG['Ei'], SMPL_PG['lam_2_corr_from_LA20K_PG'], '-o', label = '$\lambda$/2 - PG')
# plt.plot(SMPL_PG['Ei'], SMPL_PG['lam_2_corr_from_LA20K_Cu'], '-o', label = '$\lambda$/2 - Cu')
# plt.plot(SMPL_PG['Ei'], SMPL_PG['lam_3_corr_from_LA20K_Cu'], '-o', label = '$\lambda$/3 - Cu')
# plt.legend()
# plt.xlim([8.9,55])

plt.scatter(SMPL_Cu['Ei'],SMPL_Cu['signal'])
plt.scatter(SMPL_Cu['Ei'],SMPL_Cu['signal_bkg_sub'])


plt.scatter(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < SMPL_PG['Ei'].max()/4], np.interp(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < SMPL_PG['Ei'].max()/4], SMPL_PG.iloc[:,0]/4, SMPL_PG.iloc[:,1] /4 ))
plt.scatter(SMPL_PG.iloc[:,0]/4, SMPL_PG.iloc[:,1] /4 )
plt.xlim([8.9,17.5])

# PG_LOW = 26.82
# EMPTY_PG = import_fn("95672.xys", "95673.xys", "95674.xys")

# int_signal = np.interp(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,0][EMPTY_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,1][EMPTY_PG['Ei'] < PG_LOW])

# plt.scatter(SMPL_PG.iloc[:,0][SMPL_PG['Ei'] < PG_LOW], int_signal)
# plt.scatter(EMPTY_PG.iloc[:,0][EMPTY_PG['Ei'] < PG_LOW], EMPTY_PG.iloc[:,1][EMPTY_PG['Ei'] < PG_LOW])

